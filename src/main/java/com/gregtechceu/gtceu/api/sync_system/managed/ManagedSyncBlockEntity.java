package com.gregtechceu.gtceu.api.sync_system.managed;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.sync_system.SyncDataHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.connection.ConnectionType;

import io.netty.buffer.Unpooled;
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
    protected final void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.merge(getSyncDataHolder().serializeNBT(registries));
    }

    /**
     * Loads BE data from world save.<br>
     * Override this to add logic for modifying saved data before it is loaded (e.g. for cross-version
     * compatibility).<br>
     * When overriding, {@code super.load(tag)} must be called <b>AFTER</b> any custom logic.
     *
     * @param tag        The tag to load
     * @param registries Registry lookup
     */
    @Override
    @MustBeInvokedByOverriders
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        getSyncDataHolder().deserializeNBT(registries, tag);
    }

    @Override
    public final void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        byte[] data = tag.getByteArray("data");
        var buffer = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data), (RegistryAccess) lookupProvider,
                ConnectionType.NEOFORGE);
        getSyncDataHolder().readClientPacket(lookupProvider, buffer);
    }

    @Override
    public final void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt,
                                   HolderLookup.Provider lookupProvider) {
        CompoundTag tag = pkt.getTag();
        byte[] data = tag.getByteArray("data");
        var buffer = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data), (RegistryAccess) lookupProvider,
                ConnectionType.NEOFORGE);
        getSyncDataHolder().readClientPacket(lookupProvider, buffer);
    }

    /**
     * Called to gather BE data to be sent when a client loads this BE.
     */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider lookup) {
        return writeClientPacket(lookup, true);
    }

    /**
     * Called to get an update packet which is sent to clients to notify them when a loaded BE's data changes.
     */
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, (b, r) -> writeClientPacket(r, false));
    }

    private CompoundTag writeClientPacket(HolderLookup.Provider lookup, boolean fullSync) {
        var stream = new RegistryFriendlyByteBuf(Unpooled.buffer(), (RegistryAccess) lookup, ConnectionType.NEOFORGE);
        if (fullSync) getSyncDataHolder().resyncAllFields();
        getSyncDataHolder().writeClientPacket(lookup, stream);

        stream.capacity(stream.readableBytes());
        CompoundTag data = new CompoundTag();
        data.putByteArray("data", stream.array());
        return data;
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

    /**
     * Called each tick on the server side.
     */
    @MustBeInvokedByOverriders
    public void serverTick() {
        setChanged();
        if (isDirty) {
            Objects.requireNonNull(getLevel()).sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(),
                    Block.UPDATE_CLIENTS);
            isDirty = false;
        }
    }

    /**
     * Called each tick on the client side.
     */
    public void clientTick() {}
}
