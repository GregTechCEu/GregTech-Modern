package com.gregtechceu.gtceu.api.data.worldgen.ores;

import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.BulkSectionAccessAccessor;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import lombok.Getter;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Responsible for placing ores of surrounding veins for the current chunk.
 * 
 * <p>
 * Surrounding veins are resolved from the {@link OreGenCache} and placed using each block position's
 * {@link OreBlockPlacer}.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OrePlacer {

    @Getter
    private final OreGenCache oreGenCache = new OreGenCache();

    /**
     * Place the contents of all surrounding ore veins in the current chunk.
     * 
     * <p>
     * Consumes the current chunk for all of the relevant veins, allowing the cache to unload the vein,
     * once all of its chunks have been generated.
     */
    public void placeOres(WorldGenLevel level, ChunkGenerator chunkGenerator, ChunkAccess chunk) {
        if (!ConfigHolder.INSTANCE.dev.doSuperflatOres && chunkGenerator instanceof FlatLevelSource) return;

        final ChunkPos chunkPos = chunk.getPos();
        RandomSource random = new XoroshiroRandomSource(level.getSeed() ^ chunkPos.toLong());
        List<GeneratedVein> generatedVeins = oreGenCache.consumeChunkVeins(level, chunkGenerator, chunk);

        final int minSection = level.getMinSection(), maxSection = level.getMaxSection();
        try (BulkSectionAccess access = new BulkSectionAccess(level)) {
            generatedVeins.forEach(vein -> {
                for (int y = minSection; y < maxSection; y++) {
                    long sectionPos = SectionPos.asLong(chunkPos.x, y, chunkPos.z);
                    placeVein(sectionPos, random, level, access, vein, null);
                }
                placeIndicators(level, chunkPos, access, vein);
            });
        }
    }

    public void placeVein(long sectionPos, RandomSource random, WorldGenLevel level, BulkSectionAccess access,
                          GeneratedVein generatedVein, @Nullable RuleTest targetOverride) {
        LevelChunkSection section = getSection(level, access, sectionPos);
        if (section == null) return;

        // TODO readd this check
        RuleTest layerTarget = targetOverride != null ? targetOverride : generatedVein.getLayer().getTarget();

        OreBlockPlacer placer = generatedVein.consumeOres(sectionPos);
        if (placer == null) return;

        // if (layerTarget.test(blockState, random)) {
        placer.placeSectionBlocks(level, access, section, SectionPos.of(sectionPos));
        // }
    }

    private void placeIndicators(WorldGenLevel level, ChunkPos chunkPos, BulkSectionAccess access,
                                 GeneratedVein vein) {
        if (!ConfigHolder.INSTANCE.worldgen.oreVeins.oreIndicators) return;

        OreIndicatorPlacer placer = vein.consumeIndicators(chunkPos);
        if (placer == null) return;

        placer.placeIndicators(access, level);
    }

    @Nullable
    private static LevelChunkSection getSection(WorldGenLevel level, BulkSectionAccess _access, long packedSectionPos) {
        BulkSectionAccessAccessor access = (BulkSectionAccessAccessor) _access;
        int idx = level.getSectionIndexFromSectionY(SectionPos.y(packedSectionPos));
        if (idx < 0 || idx >= level.getSectionsCount()) {
            return null;
        }

        LevelChunkSection lastSection = access.getLastSection();
        if (lastSection == null || access.getLastSectionKey() != packedSectionPos) {
            lastSection = access.getAcquiredSections().computeIfAbsent(packedSectionPos, (pos) -> {
                ChunkAccess chunk = level.getChunk(SectionPos.x(pos), SectionPos.z(pos));
                LevelChunkSection section = chunk.getSection(idx);
                section.acquire();
                return section;
            });
            access.setLastSection(lastSection);
            access.setLastSectionKey(packedSectionPos);
        }

        return lastSection;
    }
}
