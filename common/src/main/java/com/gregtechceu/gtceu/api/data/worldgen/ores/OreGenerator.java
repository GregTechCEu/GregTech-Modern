package com.gregtechceu.gtceu.api.data.worldgen.ores;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureConfiguration;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreVeinSavedData;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OreGenerator {
    public record VeinConfiguration(GTOreFeatureConfiguration featureConfiguration,
                                    GTOreDefinition entry,
                                    RandomSource random,
                                    BlockPos origin) {
    }


    public Optional<GeneratedVein> generate(WorldGenLevel level, ChunkGenerator chunkGenerator, ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();

        var config = createConfig(level, chunkGenerator, chunkPos).orElse(null);
        if (config == null)
            return Optional.empty();

        GTOreDefinition entry = config.entry();
        ResourceLocation id = GTRegistries.ORE_VEINS.getKey(entry);

        if (ConfigHolder.INSTANCE.worldgen.debugWorldgen) {
            GTCEu.LOGGER.debug("trying to place vein " + id + " at " + config.origin());
        }

        var veinGenerator = entry.getVeinGenerator();
        if (veinGenerator == null)
            return Optional.empty();

        Map<BlockPos, OreBlockPlacer> generated = veinGenerator.generate(level, config.random(), entry, config.origin());

        if (generated.isEmpty()) {
            logPlaced(id, false);
            return Optional.empty();
        }

        logPlaced(id, true);

        if (ConfigHolder.INSTANCE.machines.doBedrockOres) {
            BedrockOreVeinSavedData.getOrCreate(level.getLevel()).createVein(new ChunkPos(config.origin()), entry);
        }

        return Optional.of(new GeneratedVein(chunkPos, generated));
    }


    private Optional<VeinConfiguration> createConfig(WorldGenLevel level, ChunkGenerator generator, ChunkPos chunkPos) {
        int gridSize = ConfigHolder.INSTANCE.worldgen.oreVeinGridSize;

        if (chunkPos.x % gridSize != 0 || chunkPos.z % gridSize != 0)
            return Optional.empty();

        var random = new XoroshiroRandomSource(level.getSeed() ^ chunkPos.toLong());
        var config = new GTOreFeatureConfiguration(null);

        BlockPos chunkCenter = chunkPos.getMiddleBlockPosition(0);
        GTOreDefinition entry = config.getEntry(
                level,
                level.getBiome(chunkCenter),
                random
        );

        BlockPos origin = entry.getRange().getPositions(
                new PlacementContext(level, generator, Optional.empty()),
                random, chunkCenter
        ).findFirst().orElse(chunkCenter);

        config.setEntry(null);

        return Optional.of(new VeinConfiguration(config, entry, random, origin));
    }


    private void logPlaced(ResourceLocation entry, boolean didPlace) {
        if (ConfigHolder.INSTANCE.worldgen.debugWorldgen)
            GTCEu.LOGGER.debug("Did place vein " + entry + ": " + didPlace);
    }
}
