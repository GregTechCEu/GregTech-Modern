package com.gregtechceu.gtceu.api.pipenet.longdistance;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Blocks or {@link ILDEndpoint}'s that can be part of a ld network should implement this interface.
 */
public interface ILDNetworkPart {

    /**
     * @return the long distance pipe type of this part (f.e. item or fluid)
     */
    @NotNull
    LongDistancePipeType getPipeType();

    @Nullable
    static ILDNetworkPart tryGet(LevelAccessor world, BlockPos pos) {
        return tryGet(world, pos, world.getBlockState(pos));
    }

    @Nullable
    static ILDNetworkPart tryGet(LevelAccessor world, BlockPos pos, BlockState blockState) {
        return blockState.getBlock() instanceof ILDNetworkPart part ? part : ILDEndpoint.tryGet(world, pos);
    }
}
