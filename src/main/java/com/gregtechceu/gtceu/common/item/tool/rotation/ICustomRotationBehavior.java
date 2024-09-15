package com.gregtechceu.gtceu.common.item.tool.rotation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public interface ICustomRotationBehavior {

    /**
     * Custom implementation of {@link BlockState#rotate(Rotation)} for when that behavior isn't
     * ideal.
     */
    boolean customRotate(BlockState state, Level world, BlockPos pos, BlockHitResult hitResult);

    /** Whether to show the 9-sectioned highlight grid when looking at this block while holding a Wrench. */
    default boolean showGrid() {
        return true;
    }

    /** Whether to draw an X on a provided side in the 9-sectioned highlight grid. */
    default boolean showSideTip(BlockState state, Direction side) {
        return false;
    }
}
