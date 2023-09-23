package com.gregtechceu.gtceu.api.data.worldgen.ores;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.config.ConfigHolder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;


/**
 * Used for caching ore veins between generated chunks.
 * 
 * <p>Uses the {@link OreGenerator} to generate new veins in case no vein is cached for a queried chunk.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OreGenCache {
    private final OreGenerator oreGenerator = new OreGenerator();

    private final Cache<ChunkPos, List<GeneratedVein>> generatedVeinsByOrigin = CacheBuilder.newBuilder()
            .maximumSize(ConfigHolder.INSTANCE != null ? ConfigHolder.INSTANCE.worldgen.oreVeins.oreGenerationChunkCacheSize : 512)
            .softValues()
            .build();


    /**
     * Get (or create) all veins to be generated, surrounding the supplied chunk.
     * 
     * <p>The search radius depends on the largest registered vein size, as well as the relevant config options.
     */
    public List<GeneratedVein> consumeChunk(WorldGenLevel level, ChunkGenerator generator, ChunkAccess chunk) {
        var generatedVeins = getOrCreateSurroundingVeins(level, generator, chunk);

        generatedVeins.stream()
                .filter(GeneratedVein::isFullyConsumed)
                .forEach(vein -> generatedVeinsByOrigin.invalidate(vein.getOrigin()));

        return generatedVeins;
    }

    private List<GeneratedVein> getOrCreateSurroundingVeins(WorldGenLevel level, ChunkGenerator generator, ChunkAccess chunk) {
        return getSurroundingChunks(chunk.getPos()).flatMap(chunkPos -> {
            try {
                return generatedVeinsByOrigin
                        .get(chunkPos, () -> oreGenerator.generate(level, generator, chunkPos))
                        .stream();
            } catch (ExecutionException e) {
                GTCEu.LOGGER.error("Cannot create vein in chunk " + chunkPos, e);
                return Stream.empty();
            }
        }).filter(Objects::nonNull).toList();
    }

    private Stream<ChunkPos> getSurroundingChunks(ChunkPos center) {
        int maxVeinSearchDistance = OreVeinUtil.getMaxVeinSearchDistance();

        final int minX = center.x - maxVeinSearchDistance;
        final int minZ = center.z - maxVeinSearchDistance;

        final int maxX = center.x + maxVeinSearchDistance;
        final int maxZ = center.z + maxVeinSearchDistance;

        MutableInt x = new MutableInt(minX - 1);
        MutableInt z = new MutableInt(minZ);

        return Stream.generate(() -> {
            if (x.incrementAndGet() <= maxX) {
                return new ChunkPos(x.getValue(), z.getValue());
            }

            if (z.incrementAndGet() <= maxZ) {
                x.setValue(minX);
                return new ChunkPos(x.getValue(), z.getValue());
            }

            return null;
        }).takeWhile(Objects::nonNull);
    }
}
