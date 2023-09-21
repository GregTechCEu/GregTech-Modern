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


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OrePlacer {
    private final OreGenCache oreGenCache = new OreGenCache();

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
