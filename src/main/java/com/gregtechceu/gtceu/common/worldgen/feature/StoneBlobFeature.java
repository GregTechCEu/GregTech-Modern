package com.gregtechceu.gtceu.common.worldgen.feature;

import com.gregtechceu.gtceu.common.worldgen.feature.configurations.StoneBlobConfiguration;

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
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import java.util.function.Function;

public class StoneBlobFeature extends Feature<StoneBlobConfiguration> {

    public StoneBlobFeature() {
        super(StoneBlobConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<StoneBlobConfiguration> context) {
        RandomSource random = context.random();
        BlockPos blockpos = context.origin();
        WorldGenLevel level = context.level();
        StoneBlobConfiguration config = context.config();

        int placedAmount = 0;
        int size = config.size().sample(random);
        int radius = Mth.ceil(size / 2f);
        int x0 = blockpos.getX() - radius;
        int y0 = blockpos.getY() - radius;
        int z0 = blockpos.getZ() - radius;
        int width = size + 1;
        int length = size + 1;
        int height = size + 1;

        if (blockpos.getY() >= level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, blockpos.getX(), blockpos.getZ())) {
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

                        mutablePos.set(currentX, currentY, currentZ);
                        if (!level.ensureCanWrite(mutablePos))
                            continue;
                        LevelChunkSection levelchunksection = bulkSectionAccess.getSection(mutablePos);
                        if (levelchunksection == null)
                            continue;

                        int sectionX = SectionPos.sectionRelative(currentX);
                        int sectionY = SectionPos.sectionRelative(currentY);
                        int sectionZ = SectionPos.sectionRelative(currentZ);
                        BlockState blockstate = levelchunksection.getBlockState(sectionX, sectionY, sectionZ);

                        if (!canPlaceOre(blockstate, bulkSectionAccess::getBlockState, random, config.state(),
                                mutablePos))
                            continue;
                        if (config.state().state.isAir())
                            continue;
                        levelchunksection.setBlockState(sectionX, sectionY, sectionZ, config.state().state, false);
                        ++placedAmount;
                    }
                }
            }
        }

        return placedAmount > 0;
    }

    public boolean canPlaceOre(BlockState state, Function<BlockPos, BlockState> adjacentStateAccessor,
                               RandomSource random, OreConfiguration.TargetBlockState targetState,
                               BlockPos.MutableBlockPos mutablePos) {
        if (!targetState.target.test(state, random))
            return false;

        return !isAdjacentToAir(adjacentStateAccessor, mutablePos);
    }
}
