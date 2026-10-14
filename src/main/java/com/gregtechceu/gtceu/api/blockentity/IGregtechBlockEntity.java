package com.gregtechceu.gtceu.api.blockentity;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.sync_system.managed.ISyncManaged;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.extensions.IForgeBlockEntity;

import org.jetbrains.annotations.Nullable;

public interface IGregtechBlockEntity extends ISyncManaged, ITickSubscription, IForgeBlockEntity {

    default BlockEntity self() {
        return (BlockEntity) this;
    }

    long getOffsetTimer();

    /**
     * Called to notify neighboring blocks that this block has changed.
     */
    default void notifyBlockUpdate() {
        var level = self().getLevel();
        var pos = self().getBlockPos();
        if (level != null) {
            level.updateNeighborsAt(pos, self().getBlockState().getBlock());
        }
    }

    default void scheduleNeighborShapeUpdate() {
        Level level = self().getLevel();
        BlockPos pos = self().getBlockPos();

        if (level == null)
            return;

        level.getBlockState(pos).updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
    }

    default boolean isRemote() {
        Level level = self().getLevel();
        return level == null ? GTCEu.isClientThread() : level.isClientSide;
    }

    default void scheduleRenderUpdate() {
        var pos = self().getBlockPos();
        var level = self().getLevel();
        if (level != null) {
            var state = level.getBlockState(pos);
            if (level.isClientSide) {
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_IMMEDIATE);
                requestModelDataUpdate();
            } else {
                level.blockEvent(pos, state.getBlock(), 1, 0);
            }
        }
    }

    default @Nullable BlockEntity getNeighbor(Direction direction) {
        Level level = self().getLevel();
        if (level == null) return null;
        return level.getBlockEntity(self().getBlockPos().relative(direction));
    }
}
