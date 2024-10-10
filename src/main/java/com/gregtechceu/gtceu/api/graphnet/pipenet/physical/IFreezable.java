package com.gregtechceu.gtceu.api.graphnet.pipenet.physical;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public interface IFreezable {

    /**
     * Called when the block should be fully frozen.
     */
    default void fullyFreeze(BlockState state, Level world, BlockPos pos) {
        assert Blocks.FROSTED_ICE != null;
        world.setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
    }
}
