package com.gregtechceu.gtceu.api.data.worldgen.ores;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.gregtechceu.gtceu.GTCEu;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OreGenCache {
    private final OreGenerator oreGenerator = new OreGenerator();

    private final Cache<ChunkPos, Optional<GeneratedVein>> generatedVeinsByOrigin = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .softValues()
            .concurrencyLevel(1)
            .removalListener(rem -> {
                GTCEu.LOGGER.info("~ ~ ~ ~ invalidated: " + rem.getKey());
            })
            .build();

    public List<GeneratedVein> consumeChunk(WorldGenLevel level, ChunkGenerator generator, ChunkAccess chunk) {
        var generatedVeins = getOrCreateSurroundingVeins(level, generator, chunk);

        generatedVeins.stream()
                .filter(GeneratedVein::isFullyConsumed)
                .forEach(vein -> generatedVeinsByOrigin.invalidate(vein.getOrigin()));

        return generatedVeins;
    }

    private List<GeneratedVein> getOrCreateSurroundingVeins(WorldGenLevel level, ChunkGenerator generator, ChunkAccess chunk) {
        GTCEu.LOGGER.info("getOrCreateSurroundingVeins: " + chunk.getPos());

        return getSurroundingChunks(chunk.getPos()).map(chunkPos -> {
            try {
                return generatedVeinsByOrigin
                        .get(chunkPos, () -> oreGenerator.generate(level, generator, chunkPos))
                        .map(vein -> {
                            GTCEu.LOGGER.info("    ++  Generated vein: " + vein);
                            return vein;
                        })
                        .orElseGet(() -> {
                            GTCEu.LOGGER.info("    -   No vein: " + chunkPos);
                            return null;
                        });
            } catch (ExecutionException e) {
                GTCEu.LOGGER.error("Cannot create vein in chunk " + chunkPos, e);
                return null;
            }
        }).filter(Objects::nonNull).filter(x -> {
            GTCEu.LOGGER.info("    <<< Cached ore vein: " + x);

            return true;
        }).toList();
    }

    private Stream<ChunkPos> getSurroundingChunks(ChunkPos center) {
        // TODO set search radius to max configured ore vein size
        var maxVeinSearchDistance = 3;

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
