package com.gregtechceu.gtceu.api.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface defining access to cached neighboring tile entities to a block or tile entity
 */
public interface INeighborCache extends IHasWorldObjectAndCoords {

    /**
     * @param facing the side at which the neighbor is located
     * @return the neighboring tile entity at the side
     */
    default @Nullable BlockEntity getNeighbor(@NotNull Direction facing) {
        return getLevel().getBlockEntity(getBlockPos().relative(facing));
    }

    default @Nullable BlockEntity getNeighborNoChunkloading(@NotNull Direction facing) {
        BlockPos pos = getBlockPos().relative(facing);
        return getLevel().isLoaded(pos) ? getLevel().getBlockEntity(pos) : null;
    }

    /**
     * Called when an adjacent neighboring block has changed at a side in some way
     *
     * @param facing the side at which the neighbor has changed
     */
    void onNeighborChanged(@NotNull Direction facing);
}
