package com.gregtechceu.gtceu.api.block;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface IActivableBlock {

    /**
     * @param state       The block state to test.
     * @param blockEntity The block entity to test, if any.
     * @return Whether the block is considered 'active' or not.
     */
    boolean isActive(BlockState state, @Nullable BlockEntity blockEntity);

    /**
     * Set the block state's and/or the block entity's active state to {@code value}
     * @param value        The new active state.
     * @param currentState The current block state.
     * @param blockEntity  The current block entity, if any.
     * @return The new block state.
     */
    BlockState setActive(boolean value, BlockState currentState, @Nullable BlockEntity blockEntity);
}
