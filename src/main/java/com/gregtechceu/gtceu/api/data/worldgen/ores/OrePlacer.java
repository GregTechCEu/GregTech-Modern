package com.gregtechceu.gtceu.api.data.worldgen.ores;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.SaveVeinLocation;
import com.gregtechceu.gtceu.api.data.worldgen.Vein;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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
        var random = new XoroshiroRandomSource(level.getSeed() ^ chunk.getPos().toLong());
        var generatedVeins = oreGenCache.consumeChunkVeins(level, chunkGenerator, chunk);
        AtomicInteger counter = new AtomicInteger(); // to be removed, for debbuging
        generatedVeins.forEach(generatedVein -> {

            GTCEu.LOGGER.info("Vein at %s, i: %s".formatted(generatedVein.getOrigin(), counter)); // to be removed, for debbuging
            counter.getAndIncrement(); // to be removed, for debbuging
            List<Block> testBlock = new ArrayList<>();
            testBlock.add(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(GTCEu.MOD_ID, "gtceu:iron_ring")));
            SaveVeinLocation.get(level.getLevel()).saveVein(chunk.getPos(), new Vein(testBlock));

        });
        var generatedIndicators = oreGenCache.consumeChunkIndicators(level, chunkGenerator, chunk);

        try (BulkSectionAccess access = new BulkSectionAccess(level)) {
            generatedVeins.forEach(generatedVein -> placeVein(chunk, random, access, generatedVein));
            generatedIndicators.forEach(generatedIndicator -> placeIndicators(chunk, access, generatedIndicator));
        }
    }

    private void placeVein(ChunkAccess chunk, RandomSource random, BulkSectionAccess access, GeneratedVein generatedVein) {
        RuleTest layerTarget = generatedVein.getLayer().getTarget();

        resolvePlacerLists(chunk, generatedVein).forEach(((sectionPos, placers) -> {
            LevelChunkSection section = access.getSection(sectionPos.origin());

            if (section == null)
                return;

            placers.forEach((pos, placer) -> {
                var blockState = section.getBlockState(
                        SectionPos.sectionRelative(pos.getX()),
                        SectionPos.sectionRelative(pos.getY()),
                        SectionPos.sectionRelative(pos.getZ())
                );

                if (layerTarget.test(blockState, random))
                    placer.placeBlock(access, section);
            });
        }));
    }

    private Map<SectionPos, Map<BlockPos, OreBlockPlacer>> resolvePlacerLists(ChunkAccess chunk, GeneratedVein vein) {
        return vein.consumeOres(chunk.getPos()).entrySet().stream()
                .collect(Collectors.groupingBy(
                        entry -> SectionPos.of(entry.getKey()),
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
                ));
    }

    private void placeIndicators(ChunkAccess chunk, BulkSectionAccess access, GeneratedIndicators generatedVein) {
        generatedVein.consumeIndicators(chunk.getPos()).forEach(placer -> {
            placer.placeIndicators(access);
        });
    }
}
