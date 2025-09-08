package com.gregtechceu.gtceu.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

@Deprecated(forRemoval = true)
public interface IAppearance {

    /**
     * get Appearance. same as IForgeBlock.getAppearance() / IFabricBlock.getAppearance()
     */
    @Nullable
    default BlockState getBlockAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side,
                                          BlockState sourceState, BlockPos sourcePos) {
        return state;
    }
}
