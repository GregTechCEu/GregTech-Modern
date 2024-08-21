package com.gregtechceu.gtceu.api.graphnet.pipenet.physical;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IFreezable {

    /**
     * Called when the block should be fully frozen.
     */
    default void fullyFreeze(IBlockState state, World world, BlockPos pos) {
        assert Blocks.FROSTED_ICE != null;
        world.setBlockState(pos, Blocks.FROSTED_ICE.getDefaultState());
    }
}
