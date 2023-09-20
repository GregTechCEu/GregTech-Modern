package com.gregtechceu.gtceu.api.data.worldgen.ores;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.gregtechceu.gtceu.GTCEu;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OreGenCache {
    private final OreGenerator oreGenerator = new OreGenerator();

    private final Cache<ChunkPos, Optional<GeneratedVein>> generatedVeinsByOrigin = CacheBuilder.newBuilder()
            .softValues()
            .build();

    public List<GeneratedVein> consumeChunk(WorldGenLevel level, ChunkGenerator generator, ChunkAccess chunk) {
        var generatedVeins = getOrCreateSurroundingVeins(level, generator, chunk);

        generatedVeinsByOrigin.invalidateAll(
                generatedVeins.stream().filter(GeneratedVein::isFullyConsumed).toList()
        );

        return generatedVeins;
    }

    private List<GeneratedVein> getOrCreateSurroundingVeins(WorldGenLevel level, ChunkGenerator generator, ChunkAccess chunk) {
        return getSurroundingChunks(chunk.getPos()).map(chunkPos -> {
            try {
                return generatedVeinsByOrigin
                        .get(chunkPos, () -> oreGenerator.generate(level, generator, chunk))
                        .orElse(null);
            } catch (ExecutionException e) {
                GTCEu.LOGGER.error("Cannot create vein in chunk " + chunkPos, e);
                return null;
            }
        }).filter(Objects::nonNull).toList();
    }

    private Stream<ChunkPos> getSurroundingChunks(ChunkPos center) {
        // TODO implement searching for veins SURROUNDING the chunk (search radius: max configured ore vein size)
        return Stream.of(center);
    }
}
