package com.gregtechceu.gtceu.api.data.worldgen.ores;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;

import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OrePlacer {
    private final OreGenerator oreGenerator = new OreGenerator();

    public void placeOres(WorldGenLevel level, ChunkGenerator chunkGenerator, ChunkAccess chunk) {
        var generatedVein = oreGenerator.generate(level, chunkGenerator, chunk);

        // TODO decouple generation and placement
    }
}
