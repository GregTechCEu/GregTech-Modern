package com.gregtechceu.gtceu.api.data.worldgen.ores;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OreGenCache {
    private final OreGenerator oreGenerator = new OreGenerator();

    public List<GeneratedVein> getGeneratedVeins(WorldGenLevel level, ChunkGenerator chunkGenerator, ChunkAccess chunk) {
        var generatedVein = oreGenerator.generate(level, chunkGenerator, chunk);

        // TODO implement generating veins surrounding the chunk (search radius: max configured ore vein size)

        return generatedVein.stream().toList();
    }
}
