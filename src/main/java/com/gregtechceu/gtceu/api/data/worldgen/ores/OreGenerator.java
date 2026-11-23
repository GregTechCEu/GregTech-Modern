package com.gregtechceu.gtceu.api.data.worldgen.ores;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.generator.IndicatorGenerator;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.cache.server.ServerCache;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Responsible for (pre)generating ore veins.<br>
 * This does not actually place any of the ore's blocks, and delegates to the applicable vein generator.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OreGenerator {

    public record VeinConfiguration(GeneratedVeinMetadata data, RandomSource random) {

        public RandomSource forkRandom() {
            return random.fork();
        }
    }

    public List<GeneratedVeinMetadata> generateMetadata(final WorldGenLevel level, ChunkGenerator chunkGenerator,
                                                        ChunkPos chunkPos) {
        return createConfigs(level, chunkGenerator, chunkPos).stream()
                .map(entry -> entry.data)
                .peek(OreGenerator::logVeinGeneration)
                .peek(data -> {
                    int gridX = Math.floorDiv(chunkPos.x, ConfigHolder.INSTANCE.worldgen.oreVeins.oreVeinGridSize);
                    int gridZ = Math.floorDiv(chunkPos.z, ConfigHolder.INSTANCE.worldgen.oreVeins.oreVeinGridSize);
                    ServerCache.instance.addVein(level.getLevel().dimension(), gridX, gridZ, data);
                })
                .toList();
    }

    /**
     * Generates the vein for the specified chunk metadata.<br>
     * If the chunk is not located on one of the ore vein grid's intersections, no vein will be generated.
     *
     * <p>
     * Note that depending on the configured random offset, the actual center of the generated vein may be located
     * outside the specified origin chunk.
     *
     * @return The generated vein for the specified chunk metadata.<br>
     *         {@code Optional.empty()} if no vein exists at this chunk.
     */
    public List<GeneratedVein> generateOres(WorldGenLevel level, List<GeneratedVeinMetadata> metadata,
                                            ChunkPos chunkPos) {
        return metadata.stream()
                .map(data -> {
                    RandomSource random = new XoroshiroRandomSource(level.getSeed() ^ chunkPos.toLong());
                    return new VeinConfiguration(data, random);
                })
                .flatMap(config -> generateOres(config, level, chunkPos).stream())
                .toList();
    }

    public Optional<GeneratedVein> generateOres(VeinConfiguration config, WorldGenLevel level, ChunkPos chunkPos) {
        GTOreDefinition definition = config.data.definition();
        Long2ObjectMap<OreBlockPlacer> oresBySection = definition.veinGenerator()
                .generate(level, config.forkRandom(), definition, config.data.center());

        if (oresBySection.isEmpty()) {
            logEmptyVein(config);
            return Optional.empty();
        }

        Map<ChunkPos, OreIndicatorPlacer> generatedIndicators = new HashMap<>();
        for (IndicatorGenerator gen : definition.indicatorGenerators()) {
            Map<ChunkPos, OreIndicatorPlacer> generated = gen.generate(level, config.forkRandom(), config.data);
            for (var entry : generated.entrySet()) {
                generatedIndicators.merge(entry.getKey(), entry.getValue(), (existing, added) -> {
                    return (a, l) -> {
                        existing.placeIndicators(a, l);
                        added.placeIndicators(a, l);
                    };
                });
            }
        }

        return Optional.of(new GeneratedVein(chunkPos, definition.layer(), oresBySection, generatedIndicators));
    }

    private List<VeinConfiguration> createConfigs(WorldGenLevel level, ChunkGenerator generator, ChunkPos chunkPos) {
        var random = new XoroshiroRandomSource(level.getSeed() ^ chunkPos.toLong());

        return OreVeinUtil.getVeinCenter(chunkPos, random).stream()
                .flatMap(veinCenter -> getEntries(level, veinCenter, random).map(entry -> {
                    if (entry == null) return null;
                    var id = GTRegistries.ORE_VEINS.getKey(entry);

                    BlockPos origin = computeVeinOrigin(level, generator, chunkPos, random, veinCenter, entry)
                            .orElseThrow(() -> new IllegalStateException(
                                    "Cannot determine y coordinate for the vein at " + veinCenter));

                    return new VeinConfiguration(new GeneratedVeinMetadata(id, chunkPos, origin, entry), random);
                })).toList();
    }

    private Stream<GTOreDefinition> getEntries(WorldGenLevel level, BlockPos veinCenter, XoroshiroRandomSource random) {
        return GTRegistries.WORLD_GEN_LAYERS.values().stream()
                .filter(layer -> layer.existsInDimension(level.getLevel().dimension()))
                .map(layer -> getEntry(level, level.getBiome(veinCenter), random, layer))
                .filter(Objects::nonNull);
    }

    private @Nullable GTOreDefinition getEntry(WorldGenLevel level, Holder<Biome> biome, RandomSource random,
                                               IWorldGenLayer layer) {
        var veins = WorldGeneratorUtils.getCachedBiomeVeins(level.getLevel(), biome).stream()
                .filter(vein -> vein.vein().layer().equals(layer))
                .toList();
        var randomVein = GTUtil.getRandomItem(random, veins);
        return randomVein == null ? null : randomVein.vein();
    }

    private static Optional<BlockPos> computeVeinOrigin(WorldGenLevel level, ChunkGenerator generator, ChunkPos pos,
                                                        RandomSource random, BlockPos veinCenter,
                                                        GTOreDefinition entry) {
        String layerName = GTRegistries.WORLD_GEN_LAYERS.getKey(entry.layer()).toString();
        RandomSupport.Seed128bit layerSeed = RandomSupport.seedFromHashOf(layerName);

        RandomSource layeredRandom = new XoroshiroRandomSource(random.nextLong() ^ layerSeed.seedLo(),
                random.nextLong() ^ layerSeed.seedHi());

        veinCenter = OreVeinUtil.getVeinCenter(pos, layeredRandom).orElse(veinCenter);
        PlacementContext placementContext = new PlacementContext(level, generator, Optional.empty());
        return entry.range().getPositions(placementContext, layeredRandom, veinCenter)
                .findFirst();
    }

    // /////////////////////////// //
    // ********* LOGGING ********* //
    // /////////////////////////// //

    private static void logVeinGeneration(GeneratedVeinMetadata data) {
        if (ConfigHolder.INSTANCE.dev.debugWorldgen) {
            GTCEu.LOGGER.debug("Generating vein {} at {}", data.id(), data.center());
        }
    }

    private static void logEmptyVein(VeinConfiguration config) {
        if (ConfigHolder.INSTANCE.dev.debugWorldgen) {
            GTCEu.LOGGER.debug("No blocks generated for vein {} at {}", config.data.id(), config.data.center());
        }
    }
}
