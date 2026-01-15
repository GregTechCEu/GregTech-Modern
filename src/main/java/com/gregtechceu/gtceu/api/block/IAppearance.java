package com.gregtechceu.gtceu.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

@Deprecated(forRemoval = true)
public interface IAppearance {

    /**
     * @see net.neoforged.neoforge.common.extensions.IBlockExtension#getAppearance(BlockState, BlockAndTintGetter,
     *      BlockPos, Direction, BlockState, BlockPos)
     *      IBlockExtension#getAppearance
     */
    @Nullable
    default BlockState getBlockAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side,
                                          @Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
        return state;
    }
}
