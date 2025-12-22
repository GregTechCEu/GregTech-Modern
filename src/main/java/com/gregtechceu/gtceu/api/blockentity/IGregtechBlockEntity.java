package com.gregtechceu.gtceu.api.blockentity;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.syncsystem.ISyncManaged;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlockEntity;

public interface IGregtechBlockEntity extends ISyncManaged, ITickSubscription, IForgeBlockEntity {

    Level getLevel();

    BlockPos getBlockPos();

    BlockState getBlockState();

    long getOffsetTimer();

    boolean isRemoved();

    void notifyBlockUpdate();

    default void scheduleNeighborShapeUpdate() {
        Level level = getLevel();
        BlockPos pos = getBlockPos();

        if (level == null || pos == null)
            return;

        level.getBlockState(pos).updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
    }

    void markAsChanged();

    default boolean isRemote() {
        return getLevel() == null ? GTCEu.isClientThread() : getLevel().isClientSide;
    }

    default void scheduleRenderUpdate() {
        var pos = getBlockPos();
        var level = getLevel();
        if (level != null) {
            var state = getLevel().getBlockState(pos);
            if (level.isClientSide) {
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_IMMEDIATE);
                requestModelDataUpdate();
            } else {
                level.blockEvent(pos, state.getBlock(), 1, 0);
            }
        }
    }

    default BlockEntity getNeighbor(Direction direction) {
        return getLevel().getBlockEntity(getBlockPos().relative(direction));
    }
}
