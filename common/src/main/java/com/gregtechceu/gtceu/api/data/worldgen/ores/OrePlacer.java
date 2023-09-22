package com.gregtechceu.gtceu.api.data.worldgen.ores;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Responsible for placing ores of surrounding veins for the current chunk.
 * 
 * <p>Surrounding veins are resolved from the {@link OreGenCache} and placed using each block position's {@link OreBlockPlacer}.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OrePlacer {
    private final OreGenCache oreGenCache = new OreGenCache();

    /**
     * Place the contents of all surrounding ore veins in the current chunk.
     * 
     * <p>Consumes the current chunk for all of the relevant veins, allowing the cache to unload the vein,
     * once all of its chunks have been generated.
     */
    public void placeOres(WorldGenLevel level, ChunkGenerator chunkGenerator, ChunkAccess chunk) {
        var generatedVeins = oreGenCache.consumeChunk(level, chunkGenerator, chunk);

        try (BulkSectionAccess access = new BulkSectionAccess(level)) {
            var placersBySection = generatedVeins.stream()
                    .flatMap(vein -> vein.consumeChunk(chunk.getPos()).entrySet().stream())
                    .collect(Collectors.groupingBy(
                            entry -> SectionPos.of(entry.getKey()),
                            Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                    ));

            placersBySection.forEach((sectionPos, placers) -> {
                LevelChunkSection section = access.getSection(sectionPos.origin());

                if (section == null)
                    return;

                placers.forEach(placer -> placer.placeBlock(access, section));
            });
        }
    }
}
