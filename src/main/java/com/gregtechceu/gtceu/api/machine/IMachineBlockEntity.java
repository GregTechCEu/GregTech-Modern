package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.item.tool.IToolGridHighlight;
import com.gregtechceu.gtceu.common.machine.owner.IMachineOwner;

import com.lowdragmc.lowdraglib.syncdata.blockentity.IAsyncAutoSyncBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IAutoPersistBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IRPCBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.managed.MultiManagedStorage;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * A simple compound Interface for all my TileEntities.
 * <p/>
 * Also delivers most of the Information about TileEntities.
 */
public interface IMachineBlockEntity extends IToolGridHighlight, IAsyncAutoSyncBlockEntity, IRPCBlockEntity,
                                     IAutoPersistBlockEntity {

    default BlockEntity self() {
        return (BlockEntity) this;
    }

    default Level level() {
        return self().getLevel();
    }

    default BlockPos pos() {
        return self().getBlockPos();
    }

    default void notifyBlockUpdate() {
        if (level() != null) {
            level().updateNeighborsAt(pos(), level().getBlockState(pos()).getBlock());
        }
    }

    default void scheduleRenderUpdate() {
        var pos = pos();
        if (level() != null) {
            var state = level().getBlockState(pos);
            if (level().isClientSide) {
                level().sendBlockUpdated(pos, state, state, 1 << 3);
            } else {
                level().blockEvent(pos, state.getBlock(), 1, 0);
            }
        }
    }

    default long getOffsetTimer() {
        return level() == null ? getOffset() : (level().getGameTime() + getOffset());
    }

    default MachineDefinition getDefinition() {
        if (self().getBlockState().getBlock() instanceof IMachineBlock machineBlock) {
            return machineBlock.getDefinition();
        } else {
            throw new IllegalStateException("MetaMachineBlockEntity is created for an un available block: " +
                    self().getBlockState().getBlock());
        }
    }

    MetaMachine getMetaMachine();

    long getOffset();

    MultiManagedStorage getRootStorage();

    @Override
    default void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        IAutoPersistBlockEntity.super.saveCustomPersistedData(tag, forDrop);
        getMetaMachine().saveCustomPersistedData(tag, forDrop);
    }

    @Override
    default void loadCustomPersistedData(CompoundTag tag) {
        IAutoPersistBlockEntity.super.loadCustomPersistedData(tag);
        getMetaMachine().loadCustomPersistedData(tag);
    }

    default void setOwner(IMachineOwner owner) {}

    default IMachineOwner getOwner() {
        return null;
    }
}
