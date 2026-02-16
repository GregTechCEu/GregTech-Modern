package com.gregtechceu.gtceu.api.data.worldgen.ores;

import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
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

import java.util.Map;
import java.util.stream.Collectors;

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

        var random = new XoroshiroRandomSource(level.getSeed() ^ chunk.getPos().toLong());
        var generatedVeins = oreGenCache.consumeChunkVeins(level, chunkGenerator, chunk);
        var generatedIndicators = oreGenCache.consumeChunkIndicators(level, chunkGenerator, chunk);

        try (BulkSectionAccess access = new BulkSectionAccess(level)) {
            generatedVeins.forEach(generatedVein -> placeVein(chunk.getPos(), random, access, generatedVein, null));
            generatedIndicators.forEach(generatedIndicator -> placeIndicators(chunk, access, generatedIndicator));
        }
    }

    public void placeVein(ChunkPos chunk, RandomSource random, BulkSectionAccess access,
                          GeneratedVein generatedVein, @Nullable RuleTest targetOverride) {
        RuleTest layerTarget = targetOverride != null ? targetOverride : generatedVein.getLayer().getTarget();

        resolvePlacerLists(chunk, generatedVein).forEach(((sectionPos, placers) -> {
            LevelChunkSection section = access.getSection(sectionPos.origin());

            if (section == null)
                return;

            placers.forEach((pos, placer) -> {
                var blockState = section.getBlockState(
                        SectionPos.sectionRelative(pos.getX()),
                        SectionPos.sectionRelative(pos.getY()),
                        SectionPos.sectionRelative(pos.getZ()));

                if (layerTarget.test(blockState, random))
                    placer.placeBlock(access, section);
            });
        }));
    }

    private Map<SectionPos, Map<BlockPos, OreBlockPlacer>> resolvePlacerLists(ChunkPos chunk, GeneratedVein vein) {
        return vein.consumeOres(chunk).entrySet().stream()
                .collect(Collectors.groupingBy(
                        entry -> SectionPos.of(entry.getKey()),
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private void placeIndicators(ChunkAccess chunk, BulkSectionAccess access, GeneratedIndicators generatedVein) {
        if (!ConfigHolder.INSTANCE.worldgen.oreVeins.oreIndicators) return;
        generatedVein.consumeIndicators(chunk.getPos()).forEach(placer -> {
            placer.placeIndicators(access);
        });
    }
}
