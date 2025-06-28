package com.gregtechceu.gtceu.common.worldgen.feature;

import com.gregtechceu.gtceu.common.worldgen.feature.configurations.FluidSproutConfiguration;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.material.Fluids;

import org.apache.commons.lang3.mutable.MutableInt;

import java.util.function.Function;

public class FluidSproutFeature extends Feature<FluidSproutConfiguration> {

    public FluidSproutFeature() {
        super(FluidSproutConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<FluidSproutConfiguration> context) {
        RandomSource random = context.random();
        BlockPos blockpos = context.origin();
        WorldGenLevel level = context.level();
        FluidSproutConfiguration config = context.config();

        MutableInt placedAmount = new MutableInt(0);
        int size = config.size().sample(random);
        int radius = Mth.ceil(size / 2f);
        int x0 = blockpos.getX() - radius;
        int y0 = blockpos.getY() - radius;
        int z0 = blockpos.getZ() - radius;
        int width = size + 1;
        int length = size + 1;
        int height = size + 1;

        if (config.fluid().isSame(Fluids.EMPTY)) {
            return false;
        }
        int surfaceHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, blockpos.getX(), blockpos.getZ());
        if (blockpos.getY() >= surfaceHeight) {
            return false;
        }

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        try (BulkSectionAccess bulkSectionAccess = new BulkSectionAccess(level)) {
            for (int x = 0; x < width; x++) {
                float dx = x * 2f / width - 1;
                if (dx * dx > 1)
                    continue;

                for (int y = 0; y < height; y++) {
                    float dy = y * 2f / height - 1;
                    if (dx * dx + dy * dy > 1)
                        continue;
                    if (level.isOutsideBuildHeight(y0 + y))
                        continue;

                    for (int z = 0; z < length; z++) {
                        float dz = z * 2f / length - 1;
                        if (dx * dx + dy * dy + dz * dz > 1)
                            continue;

                        final int currentX = x0 + x;
                        final int currentY = y0 + y;
                        final int currentZ = z0 + z;

                        setBlock(mutablePos, currentX, currentY, currentZ,
                                bulkSectionAccess, level,
                                config, placedAmount);
                    }
                }
            }

            if (random.nextFloat() <= config.sproutChance()) {
                int currentX = blockpos.getX();
                int currentZ = blockpos.getZ();

                int springHeight = surfaceHeight + config.surfaceOffset().sample(random);
                for (int currentY = blockpos.getY(); currentY <= springHeight; ++currentY) {
                    setBlock(mutablePos, currentX, currentY, currentZ,
                            bulkSectionAccess, level,
                            config, placedAmount);
                    if (currentY <= surfaceHeight) {
                        setBlock(mutablePos, currentX + 1, currentY, currentZ,
                                bulkSectionAccess, level,
                                config, placedAmount);
                        setBlock(mutablePos, currentX - 1, currentY, currentZ,
                                bulkSectionAccess, level,
                                config, placedAmount);
                        setBlock(mutablePos, currentX, currentY, currentZ + 1,
                                bulkSectionAccess, level,
                                config, placedAmount);
                        setBlock(mutablePos, currentX, currentY, currentZ - 1,
                                bulkSectionAccess, level,
                                config, placedAmount);
                    }
                }
            }
        }

        return placedAmount.intValue() > 0;
    }

    public void setBlock(BlockPos.MutableBlockPos mutablePos, int currentX, int currentY, int currentZ,
                         BulkSectionAccess access, WorldGenLevel level,
                         FluidSproutConfiguration config, MutableInt placedAmount) {
        mutablePos.set(currentX, currentY, currentZ);
        if (!level.ensureCanWrite(mutablePos))
            return;
        LevelChunkSection levelchunksection = access.getSection(mutablePos);
        if (levelchunksection == null)
            return;

        int sectionX = SectionPos.sectionRelative(currentX);
        int sectionY = SectionPos.sectionRelative(currentY);
        int sectionZ = SectionPos.sectionRelative(currentZ);
        levelchunksection.setBlockState(sectionX, sectionY, sectionZ,
                config.fluid().defaultFluidState().createLegacyBlock(), false);
        level.getChunk(mutablePos).markPosForPostprocessing(mutablePos);
        placedAmount.add(1);
    }

    public boolean canPlaceFluid(BlockState state, Function<BlockPos, BlockState> adjacentStateAccessor,
                                 RandomSource random, RuleTest target,
                                 BlockPos.MutableBlockPos mutablePos) {
        if (!target.test(state, random))
            return false;

        return !isAdjacentToAir(adjacentStateAccessor, mutablePos);
    }
}
