package com.gregtechceu.gtceu.api.data.worldgen.ores;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
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
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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

        public RandomSource newRandom() {
            return new XoroshiroRandomSource(random.nextLong());
        }
    }

    public List<GeneratedVeinMetadata> generateMetadata(final WorldGenLevel level, ChunkGenerator chunkGenerator,
                                                        ChunkPos chunkPos) {
        return createConfigs(level, chunkGenerator, chunkPos).stream()
                .map(OreGenerator::logVeinGeneration)
                .map(entry -> entry.data)
                .peek(data -> {
                    int gridX = Math.floorDiv(chunkPos.x, ConfigHolder.INSTANCE.worldgen.oreVeins.oreVeinGridSize);
                    int gridZ = Math.floorDiv(chunkPos.z, ConfigHolder.INSTANCE.worldgen.oreVeins.oreVeinGridSize);
                    ServerCache.instance.addVein(level.getLevel().dimension(), gridX, gridZ, data);
                })
                .toList();
    }

    public List<GeneratedIndicators> generateIndicators(WorldGenLevel level, List<GeneratedVeinMetadata> metadata,
                                                        ChunkPos chunkPos) {
        return metadata.stream()
                .map(data -> new VeinConfiguration(data,
                        new XoroshiroRandomSource(level.getSeed() ^ chunkPos.toLong())))
                .map(config -> generateIndicators(config, level, chunkPos))
                .toList();
    }

    private GeneratedIndicators generateIndicators(VeinConfiguration config, WorldGenLevel level, ChunkPos chunkPos) {
        GTOreDefinition definition = config.data.definition();

        Map<ChunkPos, List<OreIndicatorPlacer>> generatedIndicators = definition.indicatorGenerators().stream()
                .flatMap(gen -> gen.generate(level, config.newRandom(), config.data).entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey, entry -> List.of(entry.getValue()),
                        (a, b) -> Stream.of(a, b).flatMap(List::stream).toList()));

        return new GeneratedIndicators(chunkPos, generatedIndicators);
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
                .map(data -> new VeinConfiguration(
                        data,
                        new XoroshiroRandomSource(level.getSeed() ^ chunkPos.toLong())))
                .flatMap(config -> generateOres(config, level, chunkPos).stream())
                .toList();
    }

    public Optional<GeneratedVein> generateOres(VeinConfiguration config, WorldGenLevel level, ChunkPos chunkPos) {
        GTOreDefinition definition = config.data.definition();
        Map<BlockPos, OreBlockPlacer> generatedVeins = definition.veinGenerator()
                .generate(level, config.newRandom(), definition, config.data.center());

        if (generatedVeins.isEmpty()) {
            logEmptyVein(config);
            return Optional.empty();
        }

        return Optional.of(new GeneratedVein(chunkPos, definition.layer(), generatedVeins));
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
        return WorldGeneratorUtils.WORLD_GEN_LAYERS.values().stream()
                .filter(layer -> layer.isApplicableForLevel(level.getLevel().dimension().location()))
                .map(layer -> getEntry(level, level.getBiome(veinCenter), random, layer))
                .filter(Objects::nonNull);
    }

    @Nullable
    private GTOreDefinition getEntry(WorldGenLevel level, Holder<Biome> biome, RandomSource random,
                                     IWorldGenLayer layer) {
        var veins = WorldGeneratorUtils.getCachedBiomeVeins(level.getLevel(), biome).stream()
                .filter(vein -> vein.vein().layer().equals(layer))
                .toList();
        var randomVein = GTUtil.getRandomItem(random, veins);
        return randomVein == null ? null : randomVein.vein();
    }

    @NotNull
    private static Optional<BlockPos> computeVeinOrigin(WorldGenLevel level, ChunkGenerator generator, ChunkPos pos,
                                                        RandomSource random, BlockPos veinCenter,
                                                        GTOreDefinition entry) {
        int layerSeed = WorldGeneratorUtils.getWorldGenLayerKey(entry.layer())
                .map(String::hashCode)
                .orElse(0);
        var layeredRandom = new XoroshiroRandomSource(random.nextLong() ^ ((long) layerSeed));

        veinCenter = OreVeinUtil.getVeinCenter(pos, layeredRandom).orElse(veinCenter);
        return entry.range().getPositions(
                new PlacementContext(level, generator, Optional.empty()),
                layeredRandom, veinCenter).findFirst();
    }

    /////////////////////////////////////
    // ********* LOGGING *********//
    /////////////////////////////////////

    private static VeinConfiguration logVeinGeneration(VeinConfiguration config) {
        if (ConfigHolder.INSTANCE.dev.debugWorldgen) {
            GTCEu.LOGGER.debug("Generating vein {} at {}", config.data.id(), config.data.center());
        }

        return config;
    }

    private static void logEmptyVein(VeinConfiguration config) {
        if (ConfigHolder.INSTANCE.dev.debugWorldgen) {
            GTCEu.LOGGER.debug("No blocks generated for vein {} at {}", config.data.id(), config.data.center());
        }
    }
}
