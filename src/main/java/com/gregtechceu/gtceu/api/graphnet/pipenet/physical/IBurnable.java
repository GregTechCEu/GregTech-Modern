package com.gregtechceu.gtceu.api.graphnet.pipenet.physical;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public interface IBurnable {

    /**
     * Called when the block should be partially burned. <br>
     * Allows for partial burns with reference to temperature logic, used in insulated cables for example.
     */
    default void partialBurn(BlockState state, Level world, BlockPos pos) {}

    /**
     * Called when the block should be fully burned.
     */
    default void fullyBurn(BlockState state, Level world, BlockPos pos) {
        assert Blocks.FIRE != null;
        world.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
    }
}
