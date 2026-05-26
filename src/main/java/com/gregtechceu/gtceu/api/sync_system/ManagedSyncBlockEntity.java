package com.gregtechceu.gtceu.api.sync_system;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A BlockEntity that manages sync and save data via the {@code ISyncManaged} syncdata system.
 * 
 * @see ISyncManaged
 */
public abstract class ManagedSyncBlockEntity extends BlockEntity implements ISyncManaged {

    @Getter
    protected final SyncDataHolder syncDataHolder = new SyncDataHolder(this);
    @Getter
    @Setter
    private boolean isDirty;

    public ManagedSyncBlockEntity(BlockEntityCreationInfo info) {
        super(info.type(), info.pos(), info.state());
    }

    public ManagedSyncBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    /**
     * Saves BE data to world save.
     */
    @Override
    protected final void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.merge(getSyncDataHolder().serializeNBT(false));
    }

    /**
     * Loads BE data from world save.<br>
     * Override this to add logic for modifying saved data before it is loaded (e.g. for cross-version
     * compatibility).<br>
     * When overriding, {@code super.load(tag)} must be called <b>AFTER</b> any custom logic.
     *
     * @param tag The tag to load
     */
    @Override
    @MustBeInvokedByOverriders
    public void load(CompoundTag tag) {
        super.load(tag);
        getSyncDataHolder().deserializeNBT(tag, false);
    }

    /**
     * Loads BE data from client update packet
     */
    @MustBeInvokedByOverriders
    public void clientLoad(CompoundTag tag) {
        getSyncDataHolder().deserializeNBT(tag, true);
    }

    @Override
    public final void handleUpdateTag(CompoundTag tag) {
        this.clientLoad(tag);
    }

    @Override
    public final void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) clientLoad(tag);
    }

    /**
     * Called to gather BE data to be sent when a client loads this BE.
     */
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        getSyncDataHolder().resyncAllFields();
        tag.merge(getSyncDataHolder().serializeNBT(true, true));
        return tag;
    }

    /**
     * Called to get an update packet which is sent to clients to notify them when a loaded BE's data changes.
     */
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, b -> getSyncDataHolder().serializeNBT(true));
    }

    @Override
    public @Nullable ISyncManaged getParentSyncObject() {
        return null;
    }

    @Override
    public final void markAsChanged() {
        isDirty = true;
    }

    @Override
    public void setChanged() {
        if (getLevel() != null) {
            getLevel().blockEntityChanged(getBlockPos());
        }
    }

    @MustBeInvokedByOverriders
    public void serverTick() {
        setChanged();
        if (isDirty) {
            Objects.requireNonNull(getLevel()).sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(),
                    Block.UPDATE_CLIENTS);
            isDirty = false;
        }
    }
}
