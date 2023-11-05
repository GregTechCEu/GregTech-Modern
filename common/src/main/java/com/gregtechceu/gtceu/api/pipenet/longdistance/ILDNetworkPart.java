package com.gregtechceu.gtceu.api.pipenet.longdistance;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;


/**
 * Blocks or{@link ILDEndpoint}s that can be part of an LD network should implement this inerface.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface ILDNetworkPart {
    /**
     * @return the long distance pipe type of this part (e.g. item or fluid)
     */
    LongDistancePipeType getPipeType();

    @Nullable
    static ILDNetworkPart tryGet(LevelAccessor level, BlockPos pos) {
        return tryGet(level, pos, level.getBlockState(pos));
    }

    @Nullable
    static ILDNetworkPart tryGet(LevelAccessor level, BlockPos pos, BlockState blockState) {
        return blockState instanceof ILDNetworkPart part ? part : ILDEndpoint.tryGet(level, pos);
    }
}
