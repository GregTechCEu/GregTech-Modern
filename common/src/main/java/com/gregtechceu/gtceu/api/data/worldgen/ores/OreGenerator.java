package com.gregtechceu.gtceu.api.data.worldgen.ores;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureConfiguration;
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
import java.util.Map;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OreGenerator {
    private record VeinConfiguration(ResourceLocation id, GTOreDefinition entry, RandomSource random, BlockPos origin) {
    }

    public Optional<GeneratedVein> generate(WorldGenLevel level, ChunkGenerator chunkGenerator, ChunkPos chunkPos) {
        return createConfig(level, chunkGenerator, chunkPos)
                .map(OreGenerator::logVeinGeneration)
                .flatMap(config -> generate(config, level, chunkPos));
    }

    private Optional<GeneratedVein> generate(VeinConfiguration config, WorldGenLevel level, ChunkPos chunkPos) {
        Map<BlockPos, OreBlockPlacer> generated = config.entry().getVeinGenerator()
                .generate(level, config.random(), config.entry(), config.origin());

        if (generated.isEmpty()) {
            logEmptyVein(config);
            return Optional.empty();
        }

        generateBedrockOreVein(config, level);
        return Optional.of(new GeneratedVein(chunkPos, generated));
    }

    private static void generateBedrockOreVein(VeinConfiguration config, WorldGenLevel level) {
        if (ConfigHolder.INSTANCE.machines.doBedrockOres) {
            BedrockOreVeinSavedData.getOrCreate(level.getLevel()).createVein(
                    new ChunkPos(config.origin()),
                    config.entry()
            );
        }
    }

    private Optional<VeinConfiguration> createConfig(WorldGenLevel level, ChunkGenerator generator, ChunkPos chunkPos) {
        var random = new XoroshiroRandomSource(level.getSeed() ^ chunkPos.toLong());
        var config = new GTOreFeatureConfiguration();

        return OreVeinUtil.getVeinCenter(chunkPos, random).map(veinCenter -> {
            var entry = config.getEntry(level, level.getBiome(veinCenter), random);
            var id = GTRegistries.ORE_VEINS.getKey(entry);

            if (entry == null) return null;
            BlockPos origin = computeVeinOrigin(level, generator, random, veinCenter, entry).orElseThrow(() ->
                    new IllegalStateException("Cannot determine y coordinate for the vein at " + veinCenter)
            );

            return new VeinConfiguration(id, entry, random, origin);
        });
    }

    @NotNull
    private static Optional<BlockPos> computeVeinOrigin(WorldGenLevel level, ChunkGenerator generator,
                                                        XoroshiroRandomSource random,
                                                        BlockPos veinCenter, GTOreDefinition entry
    ) {
        return entry.getRange().getPositions(
                new PlacementContext(level, generator, Optional.empty()),
                random, veinCenter
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
