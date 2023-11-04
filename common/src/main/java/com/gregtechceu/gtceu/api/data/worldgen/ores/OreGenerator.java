package com.gregtechceu.gtceu.api.data.worldgen.ores;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreVeinSavedData;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * Responsible for (pre)generating ore veins.<br>
 * This does not actually place any of the ore's blocks, and delegates to the applicable vein generator.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OreGenerator {
    private record VeinConfiguration(ResourceLocation id, GTOreDefinition entry, RandomSource random, BlockPos origin) {
    }

    /**
     * Generates the vein for the specified chunk position.<br>
     * If the chunk is not located on one of the ore vein grid's intersections, no vein will be generated.
     *
     * <p>Note that depending on the configured random offset, the actual center of the generated vein may be located
     * outside the specified origin chunk.
     *
     * @return The generated vein for the specified chunk position.<br>
     * {@code Optional.empty()} if no vein exists at this chunk.
     */
    public List<GeneratedVein> generate(WorldGenLevel level, ChunkGenerator chunkGenerator, ChunkPos chunkPos) {
        return createConfigs(level, chunkGenerator, chunkPos).stream()
                .map(OreGenerator::logVeinGeneration)
                .flatMap(config -> generate(config, level, chunkPos).stream())
                .toList();
    }

    private Optional<GeneratedVein> generate(VeinConfiguration config, WorldGenLevel level, ChunkPos chunkPos) {
        Map<BlockPos, OreBlockPlacer> generated = config.entry().getVeinGenerator()
                .generate(level, config.random(), config.entry(), config.origin());

        if (generated.isEmpty()) {
            logEmptyVein(config);
            return Optional.empty();
        }

        generateBedrockOreVein(config, level);
        return Optional.of(new GeneratedVein(chunkPos, config.entry().getLayer(), generated));
    }

    private static void generateBedrockOreVein(VeinConfiguration config, WorldGenLevel level) {
        if (ConfigHolder.INSTANCE.machines.doBedrockOres) {
            BedrockOreVeinSavedData.getOrCreate(level.getLevel()).createVein(
                    new ChunkPos(config.origin()),
                    config.entry()
            );
        }
    }

    private List<VeinConfiguration> createConfigs(WorldGenLevel level, ChunkGenerator generator, ChunkPos chunkPos) {
        var random = new XoroshiroRandomSource(level.getSeed() ^ chunkPos.toLong());

        return OreVeinUtil.getVeinCenter(chunkPos, random).stream().flatMap(veinCenter ->
                getEntries(level, veinCenter, random).map(entry -> {
                    var id = GTRegistries.ORE_VEINS.getKey(entry);

                    if (entry == null) return null;
                    BlockPos origin = computeVeinOrigin(level, generator, random, veinCenter, entry).orElseThrow(() ->
                            new IllegalStateException("Cannot determine y coordinate for the vein at " + veinCenter)
                    );

                    return new VeinConfiguration(id, entry, random, origin);
                })
        ).toList();
    }

    private Stream<GTOreDefinition> getEntries(WorldGenLevel level, BlockPos veinCenter, XoroshiroRandomSource random) {
        return WorldGeneratorUtils.WORLD_GEN_LAYERS.values().stream()
                .filter(layer -> layer.isApplicableForLevel(level.getLevel().dimension().location()))
                .map(layer -> getEntry(level, level.getBiome(veinCenter), random, layer))
                .filter(Objects::nonNull);
    }

    @Nullable
    private GTOreDefinition getEntry(WorldGenLevel level, Holder<Biome> biome, RandomSource random, IWorldGenLayer layer) {
        var veins = WorldGeneratorUtils.getCachedBiomeVeins(level.getLevel(), biome, random).stream()
                .filter(vein -> vein.getValue().getLayer().equals(layer))
                .toList();
        int randomEntryIndex = GTUtil.getRandomItem(random, veins, veins.size());
        return randomEntryIndex == -1 ? null : veins.get(randomEntryIndex).getValue();
    }

    @NotNull
    private static Optional<BlockPos> computeVeinOrigin(WorldGenLevel level, ChunkGenerator generator,
                                                        RandomSource random, BlockPos veinCenter, GTOreDefinition entry
    ) {
        int layerSeed = WorldGeneratorUtils.getWorldGenLayerKey(entry.getLayer())
                .map(String::hashCode)
                .orElse(0);
        var layeredRandom = new XoroshiroRandomSource(random.nextLong() ^ ((long) layerSeed));

        return entry.getRange().getPositions(
                new PlacementContext(level, generator, Optional.empty()),
                layeredRandom, veinCenter
        ).findFirst();
    }


    /////////////////////////////////////
    //*********    LOGGING    *********//
    /////////////////////////////////////

    private static VeinConfiguration logVeinGeneration(VeinConfiguration config) {
        if (ConfigHolder.INSTANCE.dev.debugWorldgen) {
            GTCEu.LOGGER.debug("Generating vein " + config.id() + " at " + config.origin());
        }

        return config;
    }

    private static void logEmptyVein(VeinConfiguration config) {
        if (ConfigHolder.INSTANCE.dev.debugWorldgen) {
            GTCEu.LOGGER.debug("No blocks generated for vein " + config.id() + " at " + config.origin());
        }
    }
}
