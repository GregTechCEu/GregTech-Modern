package com.gregtechceu.gtceu.api.graphnet.pipenet.physical;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IBurnable {

    /**
     * Called when the block should be partially burned. <br>
     * Allows for partial burns with reference to temperature logic, used in insulated cables for example.
     */
    default void partialBurn(IBlockState state, World world, BlockPos pos) {}

    /**
     * Called when the block should be fully burned.
     */
    default void fullyBurn(IBlockState state, World world, BlockPos pos) {
        assert Blocks.FIRE != null;
        world.setBlockState(pos, Blocks.FIRE.getDefaultState());
    }
}
