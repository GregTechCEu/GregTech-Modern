package com.gregtechceu.gtceu.api.blockentity;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.sync_system.managed.ISyncManaged;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlockEntity;

public interface IGregtechBlockEntity extends ISyncManaged, ITickSubscription, IForgeBlockEntity {

    Level GTGetLevel();

    BlockPos getBlockPos();

    BlockState getBlockState();

    long getOffsetTimer();

    boolean isRemoved();

    /**
     * Called to notify neighboring blocks that this block has changed.
     */
    default void notifyBlockUpdate() {
        if (GTGetLevel() != null) {
            GTGetLevel().updateNeighborsAt(getBlockPos(), GTGetLevel().getBlockState(getBlockPos()).getBlock());
        }
    }

    default void scheduleNeighborShapeUpdate() {
        Level level = GTGetLevel();
        BlockPos pos = getBlockPos();

        if (level == null || pos == null)
            return;

        level.getBlockState(pos).updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
    }

    default boolean isRemote() {
        return GTGetLevel() == null ? GTCEu.isClientThread() : GTGetLevel().isClientSide;
    }

    default void scheduleRenderUpdate() {
        var pos = getBlockPos();
        var level = GTGetLevel();
        if (level != null) {
            var state = GTGetLevel().getBlockState(pos);
            if (level.isClientSide) {
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_IMMEDIATE);
                requestModelDataUpdate();
            } else {
                level.blockEvent(pos, state.getBlock(), 1, 0);
            }
        }
    }

    default BlockEntity getNeighbor(Direction direction) {
        return GTGetLevel().getBlockEntity(getBlockPos().relative(direction));
    }
}
