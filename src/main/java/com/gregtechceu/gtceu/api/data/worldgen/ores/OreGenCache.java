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

    private final int oreGenerationCacheSize = ConfigHolder.INSTANCE != null ?
            ConfigHolder.INSTANCE.worldgen.oreVeins.oreGenerationChunkCacheSize : 512;

    private final int oreIndicatorCacheSize = ConfigHolder.INSTANCE != null ?
            ConfigHolder.INSTANCE.worldgen.oreVeins.oreIndicatorChunkCacheSize : 512;

    private final int veinMetadataCacheSize = Math.max(oreGenerationCacheSize, oreIndicatorCacheSize);

    private final Cache<ChunkPos, List<GeneratedVeinMetadata>> veinMetadataByOrigin = CacheBuilder.newBuilder()
            .maximumSize(veinMetadataCacheSize)
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .softValues()
            .build();

    private final Cache<ChunkPos, List<GeneratedVein>> generatedVeinsByOrigin = CacheBuilder.newBuilder()
            .maximumSize(oreGenerationCacheSize)
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .softValues()
            .build();

    private final Cache<ChunkPos, List<GeneratedIndicators>> indicatorsByOrigin = CacheBuilder.newBuilder()
            .maximumSize(oreIndicatorCacheSize)
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .softValues()
            .build();

    private List<GeneratedVeinMetadata> getOrCreateVeinMetadata(WorldGenLevel level, ChunkGenerator generator,
                                                                ChunkPos chunkPos) {
        try {
            return veinMetadataByOrigin
                    .get(chunkPos, () -> oreGenerator.generateMetadata(level, generator, chunkPos));
        } catch (ExecutionException e) {
            GTCEu.LOGGER.error("Cannot create vein position in chunk {}", chunkPos, e);
            return List.of();
        }
    }

    /**
     * Get (or create) all veins to be generated, surrounding the supplied chunk.
     *
     * <p>
     * The search radius depends on the largest registered vein size, as well as the relevant config options.
     */
    public List<GeneratedVein> consumeChunkVeins(WorldGenLevel level, ChunkGenerator generator, ChunkAccess chunk) {
        return getSurroundingChunks(chunk.getPos(), OreVeinUtil.getMaxVeinSearchDistance()).flatMap(chunkPos -> {
            try {
                return generatedVeinsByOrigin
                        .get(chunkPos,
                                () -> oreGenerator.generateOres(level,
                                        getOrCreateVeinMetadata(level, generator, chunkPos), chunkPos))
                        .stream();
            } catch (ExecutionException e) {
                GTCEu.LOGGER.error("Cannot create vein in chunk {}", chunkPos, e);
                return Stream.empty();
            }
        }).filter(Objects::nonNull).toList();
    }

    /**
     * Get (or create) all indicators to be generated, surrounding the supplied chunk.
     *
     * <p>
     * The search radius depends on the largest registered indicator size, as well as the relevant config options.
     */
    public List<GeneratedIndicators> consumeChunkIndicators(WorldGenLevel level, ChunkGenerator generator,
                                                            ChunkAccess chunk) {
        return getSurroundingChunks(chunk.getPos(), OreVeinUtil.getMaxIndicatorSearchDistance()).flatMap(chunkPos -> {
            try {
                return indicatorsByOrigin
                        .get(chunkPos,
                                () -> oreGenerator.generateIndicators(level,
                                        getOrCreateVeinMetadata(level, generator, chunkPos), chunkPos))
                        .stream();
            } catch (ExecutionException e) {
                GTCEu.LOGGER.error("Cannot create vein in chunk {}", chunkPos, e);
                return Stream.empty();
            }
        }).filter(Objects::nonNull).toList();
    }

    private Stream<ChunkPos> getSurroundingChunks(ChunkPos center, int searchDistance) {
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
