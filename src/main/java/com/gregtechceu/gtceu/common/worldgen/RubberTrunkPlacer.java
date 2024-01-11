package com.gregtechceu.gtceu.common.worldgen;

import com.google.common.collect.ImmutableList;
import com.gregtechceu.gtceu.common.data.GTPlacerTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.BiConsumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RubberTrunkPlacer extends TrunkPlacer {
    public static final Codec<RubberTrunkPlacer> CODEC = RecordCodecBuilder.create((p_70206_) -> trunkPlacerParts(p_70206_).apply(p_70206_, RubberTrunkPlacer::new));

    public RubberTrunkPlacer(int pBaseHeight, int pHeightRandA, int pHeightRandB) {
        super(pBaseHeight, pHeightRandA, pHeightRandB);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return GTPlacerTypes.RUBBER_TRUNK;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, int freeTreeHeight, BlockPos pos, TreeConfiguration config) {
        BlockPos blockpos = pos.below();
        setDirtAt(level, blockSetter, random, blockpos, config);
        setDirtAt(level, blockSetter, random, blockpos.east(), config);
        setDirtAt(level, blockSetter, random, blockpos.south(), config);
        setDirtAt(level, blockSetter, random, blockpos.south().east(), config);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        freeTreeHeight--;
        for (int i = 0; i < freeTreeHeight; ++i) {
            placeLogIfFreeWithOffset(level, blockSetter, random, mutableBlockPos, config, pos, 0, i, 0);
            if (i < freeTreeHeight - 1) {
                placeLogIfFreeWithOffset(level, blockSetter, random, mutableBlockPos, config, pos, 1, i, 0);
                placeLogIfFreeWithOffset(level, blockSetter, random, mutableBlockPos, config, pos, -1, i, 0);
                placeLogIfFreeWithOffset(level, blockSetter, random, mutableBlockPos, config, pos, 0, i, 1);
                placeLogIfFreeWithOffset(level, blockSetter, random, mutableBlockPos, config, pos, 0, i, -1);
            }
            if (i == 0 || i == freeTreeHeight - 6) {
                placeLogIfFreeWithOffset(level, blockSetter, random, mutableBlockPos, config, pos, 1, i, 1);
                placeLogIfFreeWithOffset(level, blockSetter, random, mutableBlockPos, config, pos, 1, i, -1);
                placeLogIfFreeWithOffset(level, blockSetter, random, mutableBlockPos, config, pos, -1, i, 1);
                placeLogIfFreeWithOffset(level, blockSetter, random, mutableBlockPos, config, pos, -1, i, -1);
                placeLogIfFreeWithOffset(level, blockSetter, random, mutableBlockPos, config, pos, 2, i, 0);
                placeLogIfFreeWithOffset(level, blockSetter, random, mutableBlockPos, config, pos, -2, i, 0);
                placeLogIfFreeWithOffset(level, blockSetter, random, mutableBlockPos, config, pos, 0, i, 2);
                placeLogIfFreeWithOffset(level, blockSetter, random, mutableBlockPos, config, pos, 0, i, -2);
            }
            if (i == 1 || i == freeTreeHeight - 7) {
                placeLogIfFreeWithOffsetAndChance(0.33f, level, blockSetter, random, mutableBlockPos, config, pos, 1, i, 1);
                placeLogIfFreeWithOffsetAndChance(0.33f, level, blockSetter, random, mutableBlockPos, config, pos, 1, i, -1);
                placeLogIfFreeWithOffsetAndChance(0.33f, level, blockSetter, random, mutableBlockPos, config, pos, -1, i, 1);
                placeLogIfFreeWithOffsetAndChance(0.33f, level, blockSetter, random, mutableBlockPos, config, pos, -1, i, -1);

            }
        }
        return ImmutableList.of(new FoliagePlacer.FoliageAttachment(pos.above(freeTreeHeight), 0, false));
    }

    private void placeLogIfFreeWithOffset(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, BlockPos.MutableBlockPos pos, TreeConfiguration config, BlockPos pOffsetPos, int pOffsetX, int pOffsetY, int pOffsetZ) {
        pos.setWithOffset(pOffsetPos, pOffsetX, pOffsetY, pOffsetZ);
        placeLogIfFree(level, blockSetter, random, pos, config);
    }

    private void placeLogIfFreeWithOffsetAndChance(float pChance, LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, BlockPos.MutableBlockPos pos, TreeConfiguration config, BlockPos pOffsetPos, int pOffsetX, int pOffsetY, int pOffsetZ) {
        if (random.nextFloat() > pChance) {
            return;
        }
        pos.setWithOffset(pOffsetPos, pOffsetX, pOffsetY, pOffsetZ);
        placeLogIfFree(level, blockSetter, random, pos, config);
    }
}
