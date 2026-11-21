package com.gregtechceu.gtceu.api.data.worldgen.ores;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Used for caching ore veins between generated chunks.
 * 
 * <p>
 * Uses the {@link OreGenerator} to generate new veins in case no vein is cached for a queried chunk.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OreGenCache {

    @Getter
    private final OreGenerator oreGenerator = new OreGenerator();

    private final int oreGenerationCacheSize = ConfigHolder.getInstance().worldgen.oreVeins.oreGenerationChunkCacheSize;
    private final int oreIndicatorCacheSize = ConfigHolder.getInstance().worldgen.oreVeins.oreIndicatorChunkCacheSize;
    private final int veinMetadataCacheSize = Math.max(oreGenerationCacheSize, oreIndicatorCacheSize);

    private final Cache<ChunkPos, List<GeneratedVeinMetadata>> veinMetadataByOrigin = CacheBuilder.newBuilder()
            .maximumSize(veinMetadataCacheSize)
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .softValues()
            .build();

    private final Cache<ChunkPos, List<GeneratedVein>> generatedVeinsByOrigin = CacheBuilder.newBuilder()
            // TODO figure out a better way to cache indicators and veins in the same object. it reduces memory usage
            // but increasing this makes the map way bigger than it actually has to be. I think.
            .maximumSize(veinMetadataCacheSize)
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .softValues()
            .build();

    private List<GeneratedVeinMetadata> getOrCreateVeinMetadata(WorldGenLevel level, ChunkGenerator generator,
                                                                ChunkPos chunkPos) {
        try {
            return veinMetadataByOrigin.get(chunkPos, () -> oreGenerator.generateMetadata(level, generator, chunkPos));
        } catch (ExecutionException e) {
            GTCEu.LOGGER.error("Cannot create vein position in chunk {}", chunkPos, e);
            return List.of();
        }
    }

    /**
     * Get (or create) all veins and indicators to be generated, surrounding the supplied chunk.
     *
     * <p>
     * The search radius depends on the largest registered vein size, as well as the relevant config options.
     */
    public List<GeneratedVein> consumeChunkVeins(WorldGenLevel level, ChunkGenerator generator, ChunkAccess chunk) {
        final int maxSearchDistance = OreVeinUtil.getMaxIndicatorSearchDistance();
        return getSurroundingChunks(chunk.getPos(), maxSearchDistance).flatMap(chunkPos -> {
            try {
                return generatedVeinsByOrigin.get(chunkPos, () -> {
                    List<GeneratedVeinMetadata> metadata = getOrCreateVeinMetadata(level, generator, chunkPos);
                    return oreGenerator.generateOres(level, metadata, chunkPos);
                }).stream();
            } catch (ExecutionException e) {
                GTCEu.LOGGER.error("Cannot create vein in chunk {}", chunkPos, e);
                return Stream.empty();
            }
        }).filter(Objects::nonNull).toList();
    }

    private Stream<ChunkPos> getSurroundingChunks(ChunkPos center, int searchDistance) {
        // suboptimal vanilla implementation
        // return ChunkPos.rangeClosed(center, searchDistance);
        final int minX = center.x - searchDistance;
        final int minZ = center.z - searchDistance;

        final int maxX = center.x + searchDistance;
        final int maxZ = center.z + searchDistance;

        MutableInt x = new MutableInt(minX - 1);
        MutableInt z = new MutableInt(minZ);

        return Stream.generate(() -> {
            if (x.incrementAndGet() <= maxX) {
                return new ChunkPos(x.intValue(), z.intValue());
            }

            if (z.incrementAndGet() <= maxZ) {
                x.setValue(minX);
                return new ChunkPos(x.intValue(), z.intValue());
            }

            return null;
        }).takeWhile(Objects::nonNull);
    }
}
