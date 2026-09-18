package brachy.modularui.value.sync;

import brachy.modularui.ModularUI;
import brachy.modularui.api.IPacketWriter;
import brachy.modularui.api.value.ISyncOrValue;
import brachy.modularui.network.ModularNetwork;
import brachy.modularui.network.ModularNetworkSide;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import lombok.Getter;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Base class for handling syncing of widgets.
 * A sync handler must exist on client and server.
 * It must be configured exactly the same to avoid issues.
 */
public abstract class SyncHandler<S extends SyncHandler<S>> implements ISyncOrValue {

    private PanelSyncManager syncManager;
    /**
     * the key that belongs to this sync handler
     */
    @Getter private String key;
    @Getter private boolean allowC2S;

    @ApiStatus.OverrideOnly
    @MustBeInvokedByOverriders
    public void init(String key, PanelSyncManager syncManager) {
        this.key = key;
        this.syncManager = syncManager;
    }

    @ApiStatus.OverrideOnly
    @MustBeInvokedByOverriders
    public void dispose() {
        this.key = null;
        this.syncManager = null;
    }

    /**
     * Syncs a custom packet to the client
     *
     * @param id             an internal denominator to identify this package
     * @param bufferConsumer the package builder
     */
    public final void syncToClient(int id, @NotNull IPacketWriter<? super RegistryFriendlyByteBuf> bufferConsumer) {
        IPacketWriter<? super RegistryFriendlyByteBuf> writer = buffer -> {
            VarInt.write(buffer, id);
            bufferConsumer.write(buffer);
        };
        sendToClient(getSyncManager().getPanelName(), writer, this);
    }

    /**
     * Syncs a custom packet to the server
     *
     * @param id             an internal denominator to identify this package
     * @param bufferConsumer the package builder
     */
    @OnlyIn(Dist.CLIENT)
    public final void syncToServer(int id, @NotNull IPacketWriter<? super RegistryFriendlyByteBuf> bufferConsumer) {
        if (!isAllowC2S()) {
            ModularUI.LOGGER.throwing(Level.WARN, new SecurityException("Sync handler is unable to send packets to server!"));
            return;
        }
        IPacketWriter<? super RegistryFriendlyByteBuf> writer = buffer -> {
            VarInt.write(buffer, id);
            bufferConsumer.write(buffer);
        };
        sendToServer(getSyncManager().getPanelName(), writer, this);
    }

    /**
     * Sync a custom packet to the other side.
     *
     * @param id             an internal denominator to identify this package
     * @param bufferConsumer the package builder
     */
    public final void sync(int id, @NotNull IPacketWriter<? super RegistryFriendlyByteBuf> bufferConsumer) {
        if (getSyncManager().isClient() && !isAllowC2S()) {
            ModularUI.LOGGER.throwing(Level.WARN, new SecurityException("Sync handler is unable to send packets to server!"));
            return;
        }
        IPacketWriter<? super RegistryFriendlyByteBuf> writer = buffer -> {
            VarInt.write(buffer, id);
            bufferConsumer.write(buffer);
        };
        send(ModularNetwork.get(getSyncManager().isClient()), getSyncManager().getPanelName(), writer, this);
    }

    /**
     * Sends an empty packet to the client with an id.
     *
     * @param id identifier
     */
    public final void syncToClient(int id) {
        syncToClient(id, buf -> {});
    }

    /**
     * Sends an empty packet to the server with an id.
     *
     * @param id identifier
     */
    public final void syncToServer(int id) {
        syncToServer(id, buf -> {});
    }

    /**
     * Sends an empty packet to the other side with an id.
     *
     * @param id identifier
     */
    public final void sync(int id) {
        sync(id, buf -> {});
    }

    /**
     * Called when this sync handler receives a packet on client.
     *
     * @param id  an internal denominator to identify this package
     * @param buf package
     */
    @ApiStatus.OverrideOnly
    @OnlyIn(Dist.CLIENT)
    public abstract void readOnClient(int id, RegistryFriendlyByteBuf buf);

    /**
     * Called when this sync handler receives a packet on server.
     *
     * @param id  an internal denominator to identify this package
     * @param buf package
     */
    @ApiStatus.OverrideOnly
    public abstract void readOnServer(int id, RegistryFriendlyByteBuf buf);

    /**
     * Called at least every tick. Use it to compare a cached value to its original and sync it.
     * This is only called on the server side.
     *
     * @param init if this method is being called the first time.
     */
    public void detectAndSendChanges(boolean init) {}

    /**
     * @return is this sync handler has been initialised yet
     */
    public final boolean isValid() {
        return this.key != null && this.syncManager != null;
    }

    /**
     * @return the sync handler manager handling this sync handler
     */
    public PanelSyncManager getSyncManager() {
        if (!isValid()) {
            throw new IllegalStateException("Sync handler is not yet initialised!");
        }
        return this.syncManager;
    }

    public final boolean isRegistered(PanelSyncManager syncManager) {
        if (isValid() && this.syncManager.hasSyncHandler(this)) {
            return true;
        }
        return syncManager.hasSyncHandler(this);
    }

    @Override
    public boolean isSyncHandler() {
        return true;
    }

    /**
     * Sets this sync handler to accept C2S (client to server) packets. This value MUST be the same on client and server.
     * By default, this is false to prevent clients from force updating values. Values which the player can control through a button for
     * example are completely fine to allow C2S updates.
     *
     * @param allowC2S whether this sync handler should allow client to server updates
     * @return this
     */
    public S allowC2S(boolean allowC2S) {
        this.allowC2S = allowC2S;
        return self();
    }

    /**
     * Sets this sync handler whether to accept C2S (client to server) packets or not. This value MUST be the same on client and server.
     * By default, this is false to prevent clients from force updating values. Values which the player can control through a button for
     * example are completely fine to allow C2S updates.
     *
     * @return this
     */
    public S allowC2S() {
        return allowC2S(true);
    }

    @SuppressWarnings("unchecked")
    public S self() {
        return (S) this;
    }

    private static void send(ModularNetworkSide network, String panel, IPacketWriter<? super RegistryFriendlyByteBuf> writer, SyncHandler<?> syncHandler) {
        Objects.requireNonNull(writer);
        Objects.requireNonNull(syncHandler);
        if (!syncHandler.isValid()) {
            throw new IllegalStateException("Not initialized sync handlers can't send packets!");
        }
        network.sendSyncHandlerPacket(panel, syncHandler, writer, syncHandler.syncManager.getPlayer());
    }

    public static void sendToClient(String panel, IPacketWriter<? super RegistryFriendlyByteBuf> writer, SyncHandler<?> syncHandler) {
        send(ModularNetwork.SERVER, panel, writer, syncHandler);
    }

    @OnlyIn(Dist.CLIENT)
    public static void sendToServer(String panel, IPacketWriter<? super RegistryFriendlyByteBuf> writer, SyncHandler<?> syncHandler) {
        send(ModularNetwork.CLIENT, panel, writer, syncHandler);
    }
}
