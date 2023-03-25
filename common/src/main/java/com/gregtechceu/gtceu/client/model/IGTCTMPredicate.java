package com.gregtechceu.gtceu.client.model;

import com.lowdragmc.lowdraglib.client.model.custommodel.ICTMPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * @author KilaBash
 * @date 2019/12/7
 * @implNote IGTCTMPredicate
 */
public interface IGTCTMPredicate extends ICTMPredicate {

    @Nullable
    @Override
    @Deprecated
    default ResourceLocation getConnectedID() {
        return null;
    }

    /**
     * If two blocks are adjacent and have same connected id, they can be regarded as connected.
     */
    @Nullable
    default ResourceLocation getConnectedID(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        if (this instanceof Block block) {
            return Registry.BLOCK.getKey(block);
        }
        return null;
    }

    @Override
    default boolean isConnected(BlockAndTintGetter level, BlockPos corePos, BlockState coreState, BlockPos adjacentPos, BlockState adjacentState, Direction side) {
        var adjacentID = getConnectedID(level, adjacentPos, adjacentState);
        if (adjacentID != null) {
            if (ICTMPredicate.getPredicate(coreState) instanceof IGTCTMPredicate corePredicate) {
                return adjacentID.equals(corePredicate.getConnectedID(level, corePos, coreState));
            }
        }
        return false;
    }

}
