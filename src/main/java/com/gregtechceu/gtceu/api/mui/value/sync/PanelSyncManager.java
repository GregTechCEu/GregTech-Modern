package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.IPanelHandler;
import com.gregtechceu.gtceu.api.mui.base.ISyncedAction;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;
import com.gregtechceu.gtceu.client.mui.screen.ModularContainerMenu;
import com.gregtechceu.gtceu.common.network.ModularNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PanelSyncManager implements ISyncRegistrar<PanelSyncManager> {

    private final Map<String, SyncHandler> syncHandlers = new Object2ReferenceLinkedOpenHashMap<>();
    private final Map<String, SlotGroup> slotGroups = new Object2ReferenceOpenHashMap<>();
    private final Map<SyncHandler, String> reverseSyncHandlers = new Object2ReferenceOpenHashMap<>();
    private final Map<String, SyncedAction> syncedActions = new Object2ReferenceOpenHashMap<>();
    private final Map<String, PanelSyncHandler> subPanels = new Object2ReferenceArrayMap<>();
    @Getter
    private final ModularSyncManager modularSyncManager;
    @Getter
    private String panelName;
    private boolean init = true;
    @Getter
    private boolean locked = false;
    private boolean allowSyncHandlerRegistration = false;
    @Getter
    private final boolean client;

    private final List<Consumer<Player>> openListener = new ArrayList<>();
    private final List<Consumer<Player>> closeListener = new ArrayList<>();
    private final List<Runnable> tickListener = new ArrayList<>();

    @ApiStatus.Internal
    public PanelSyncManager(ModularSyncManager msm, boolean main) {
        this.modularSyncManager = msm;
        this.client = msm.isClient();
        if (main) msm.setMainPSM(this);
    }

    @ApiStatus.Internal
    public void initialize(String panelName) {
        this.panelName = panelName;
        this.syncHandlers.forEach((mapKey, syncHandler) -> syncHandler.init(mapKey, this));
        this.locked = true;
        this.init = true;
        this.subPanels.forEach(
                (s, syncHandler) -> this.modularSyncManager.getMainPSM().registerPanelSyncHandler(s, syncHandler));
    }

    private void registerPanelSyncHandler(String name, SyncHandler syncHandler) {
        // only called on main psm
        SyncHandler currentSh = this.syncHandlers.get(name);
        if (currentSh != null && currentSh != syncHandler) {
            throw new IllegalStateException("Failed to register panel sync handler during initialization. " +
                    "There already exists a sync handler for the name '" + name + "'.");
        }
        String currentName = this.reverseSyncHandlers.get(syncHandler);
        if (currentName != null && !name.equals(currentName)) {
            throw new IllegalStateException(
                    "Failed to register panel sync handler for name '" + name + "' during initialization. " +
                            "The panel sync handler is already registered under the name '" + currentName + "'.");
        }
        this.syncHandlers.put(name, syncHandler);
        this.reverseSyncHandlers.put(syncHandler, name);
        syncHandler.init(name, this);
    }

    void closeSubPanels() {
        this.subPanels.values().forEach(syncHandler -> {
            if (syncHandler.isSubPanel()) {
                syncHandler.closePanel();
            }
        });
    }

    @ApiStatus.Internal
    public void onOpen() {
        this.openListener.forEach(listener -> listener.accept(getPlayer()));
    }

    @ApiStatus.Internal
    public void onClose() {
        this.closeListener.forEach(listener -> listener.accept(getPlayer()));
        this.syncHandlers.values().forEach(SyncHandler::dispose);
        // previously panel sync handlers were removed from the main psm, however this problematic if the screen will be
        // reopened at some point.
        // we can just not remove the sync handlers since mui has proper checks for re-registering panels
    }

    public boolean isInitialised() {
        return this.panelName != null;
    }

    void detectAndSendChanges(boolean init) {
        if (!isClient()) {
            for (SyncHandler syncHandler : this.syncHandlers.values()) {
                syncHandler.detectAndSendChanges(init || this.init);
            }
        }
        this.init = false;
    }

    void onUpdate() {
        this.tickListener.forEach(Runnable::run);
    }

    @ApiStatus.Internal
    public void receiveWidgetUpdate(String mapKey, boolean action, int id, FriendlyByteBuf buf) {
        if (action) {
            invokeSyncedAction(mapKey, buf);
            return;
        }

        if (!this.syncHandlers.containsKey(mapKey)) {
            GTCEu.LOGGER.warn("SyncHandler '{}' does not exist for panel '{}'! ID was {}.", mapKey, panelName, id);
            return;
        }
        SyncHandler syncHandler = this.syncHandlers.get(mapKey);
        if (isClient()) {
            syncHandler.readOnClient(id, buf);
        } else {
            syncHandler.readOnServer(id, buf);
        }
    }

    private boolean invokeSyncedAction(String mapKey, FriendlyByteBuf buf) {
        SyncedAction syncedAction = this.syncedActions.get(mapKey);
        if (syncedAction == null) {
            GTCEu.LOGGER.warn("SyncAction '{}' does not exist for panel '{}'!.", mapKey, panelName);
            return false;
        }
        if (!isLocked() || this.allowSyncHandlerRegistration || !syncedAction.isExecuteClient() ||
                !syncedAction.isExecuteServer()) {
            syncedAction.invoke(this.client, buf);
        } else {
            // only allow sync handler registration if it is executed on client and server
            allowTemporarySyncHandlerRegistration(true);
            syncedAction.invoke(this.client, buf);
            allowTemporarySyncHandlerRegistration(false);
        }
        // true if the action should be executed on the other side
        return syncedAction.isExecute(!this.client);
    }

    public ItemStack getCursorItem() {
        return getModularSyncManager().getCursorItem();
    }

    public void setCursorItem(ItemStack stack) {
        getModularSyncManager().setCursorItem(stack);
    }

    @Override
    public boolean hasSyncHandler(SyncHandler syncHandler) {
        return syncHandler.isValid() && syncHandler.getSyncManager() == this &&
                this.reverseSyncHandlers.containsKey(syncHandler);
    }

    private void putSyncValue(String name, int id, SyncHandler syncHandler) {
        if (isLocked()) {
            // registration of sync handlers forbidden
            if (this.allowSyncHandlerRegistration) {
                // lock can be bypassed currently, but it wasn't used
                throw new IllegalStateException(
                        "SyncHandlers must be registered during panel building. Please use getOrCreateSyncHandler() inside DynamicSyncHandler!");
            } else {
                throw new IllegalStateException(
                        "SyncHandlers must be registered during panel building. The only exceptions is via a DynamicSyncHandler and sync functions!");
            }
        }
        String key = makeSyncKey(name, id);
        String currentKey = this.reverseSyncHandlers.get(syncHandler);
        if (currentKey != null) {
            if (!currentKey.equals(key)) {
                boolean auto = name.startsWith(ModularSyncManager.AUTO_SYNC_PREFIX);
                if (auto != currentKey.startsWith(ModularSyncManager.AUTO_SYNC_PREFIX)) {
                    throw new IllegalStateException("Old and new sync handler must both be either not auto or auto!");
                }
                if (auto && !currentKey.startsWith(name)) {
                    throw new IllegalStateException("Sync Handler was previously added with a different panel!");
                }
            }
            this.syncHandlers.remove(currentKey);
        }
        this.syncHandlers.put(key, syncHandler);
        this.reverseSyncHandlers.put(syncHandler, key);
        if (isInitialised()) {
            syncHandler.init(key, this);
        }
    }

    @Override
    public PanelSyncManager syncValue(String name, int id, SyncHandler syncHandler) {
        Objects.requireNonNull(name, "Name must not be null");
        Objects.requireNonNull(syncHandler, "Sync Handler must not be null");
        putSyncValue(name, id, syncHandler);
        return this;
    }

    /**
     * @deprecated replaced by {@link #syncedPanel(String, boolean, PanelSyncHandler.IPanelBuilder)}
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "3.3.0")
    @Deprecated
    public IPanelHandler panel(String key, PanelSyncHandler.IPanelBuilder panelBuilder, boolean subPanel) {
        SyncHandler sh = this.subPanels.get(key);
        if (sh != null) return (IPanelHandler) sh;
        PanelSyncHandler syncHandler = new PanelSyncHandler(panelBuilder, subPanel);
        this.subPanels.put(key, syncHandler);
        return syncHandler;
    }

    /**
     * Creates a synced panel handler. This can be used to automatically handle syncing for synced panels.
     * Synced panels do not need to be synced themselves, but contain at least one widget which is synced.
     * <p>
     * <b>NOTE</b>
     * </p>
     * A panel sync handler is only created once. If one was already registered, that one will be returned.
     * (This is only relevant for nested sub panels.) Furthermore, the panel handler has to be created on client and
     * server with the same
     * key. Like any other sync handler, the panel sync handler has to be created before the panel opened. The only
     * exception is inside
     * dynamic sync handlers.
     *
     * @param key          the key used for syncing
     * @param subPanel     true if this panel should close when its parent closes (the parent is defined by <i>this</i>
     *                     {@link PanelSyncManager})
     * @param panelBuilder the panel builder, that will create the new panel. It must not return null or any existing
     *                     panels.
     * @return a synced panel handler.
     * @throws NullPointerException     if the build panel of the builder is null
     * @throws IllegalArgumentException if the build panel of the builder is the main panel
     * @throws IllegalStateException    if this method was called too late
     */
    @Override
    public IPanelHandler syncedPanel(String key, boolean subPanel, PanelSyncHandler.IPanelBuilder panelBuilder) {
        IPanelHandler ph = findPanelHandlerNullable(key);
        if (ph != null) return ph;
        if (isLocked() && !this.allowSyncHandlerRegistration) {
            // registration of sync handlers forbidden
            throw new IllegalStateException(
                    "Synced panels must be registered during panel building. The only exceptions is via a DynamicSyncHandler and sync functions!");
        }
        PanelSyncHandler syncHandler = new PanelSyncHandler(panelBuilder, subPanel);
        this.subPanels.put(key, syncHandler);
        if (isInitialised() && (this == this.modularSyncManager.getMainPSM() ||
                this.modularSyncManager.getMainPSM().findSyncHandlerNullable(this.panelName, PanelSyncHandler.class) ==
                        null)) {
            // current panel is open
            this.modularSyncManager.getMainPSM().registerPanelSyncHandler(key, syncHandler);
        }
        return syncHandler;
    }

    @Override
    public @Nullable IPanelHandler findPanelHandlerNullable(String key) {
        return this.subPanels.get(key);
    }

    @Override
    public PanelSyncManager registerSlotGroup(SlotGroup slotGroup) {
        if (!slotGroup.isSingleton()) {
            this.slotGroups.put(slotGroup.getName(), slotGroup);
        }
        return this;
    }

    public interface SlotFunction {

        @NotNull
        ModularSlot apply(@NotNull PlayerMainInvWrapper playerInv, int index);
    }

    public PanelSyncManager addOpenListener(Consumer<Player> listener) {
        this.openListener.add(listener);
        return this;
    }

    public PanelSyncManager addCloseListener(Consumer<Player> listener) {
        this.closeListener.add(listener);
        return this;
    }

    public PanelSyncManager onClientTick(Runnable runnable) {
        if (this.client) {
            this.tickListener.add(runnable);
        }
        return this;
    }

    public PanelSyncManager onServerTick(Runnable runnable) {
        if (!this.client) {
            this.tickListener.add(runnable);
        }
        return this;
    }

    public PanelSyncManager onCommonTick(Runnable runnable) {
        this.tickListener.add(runnable);
        return this;
    }

    @Override
    public PanelSyncManager registerSyncedAction(String mapKey, boolean executeClient, boolean executeServer,
                                                 ISyncedAction action) {
        if (executeClient || executeServer) {
            this.syncedActions.put(mapKey, new SyncedAction(action, executeClient, executeServer));
        }
        return this;
    }

    public void callSyncedAction(String mapKey, FriendlyByteBuf packet) {
        if (invokeSyncedAction(mapKey, packet)) {
            ModularNetwork.get(isClient()).sendActionPacket(getModularSyncManager(), this.panelName, mapKey, packet,
                    getPlayer());
        }
    }

    public void callSyncedAction(String mapKey, Consumer<FriendlyByteBuf> packetBuilder) {
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packetBuilder.accept(packet);
        callSyncedAction(mapKey, packet);
    }

    public void callSyncedAction(String mapKey) {
        callSyncedAction(mapKey, new FriendlyByteBuf(Unpooled.buffer(0)));
    }

    @Override
    public <T extends SyncHandler> T getOrCreateSyncHandler(String name, int id, Class<T> clazz, Supplier<T> supplier) {
        SyncHandler syncHandler = findSyncHandlerNullable(name, id);
        if (syncHandler == null) {
            if (isLocked() && !this.allowSyncHandlerRegistration) {
                // registration is locked, and we don't have permission to temporarily bypass lock
                throw new IllegalStateException(
                        "SyncHandlers must be registered during panel building. The only exceptions is via a DynamicSyncHandler and sync functions!");
            }
            T t = supplier.get();
            boolean l = this.locked;
            this.locked = false; // bypass possible lock
            putSyncValue(name, id, t);
            this.locked = l;
            return t;
        }

        if (clazz.isAssignableFrom(syncHandler.getClass())) {
            return clazz.cast(syncHandler);
        }
        throw new IllegalStateException("SyncHandler for key " + makeSyncKey(name, id) + " is of type " +
                syncHandler.getClass() + ", but type " + clazz + " was expected!");
    }

    @Override
    public SlotGroup getSlotGroup(String name) {
        return this.slotGroups.get(name);
    }

    public Collection<SlotGroup> getSlotGroups() {
        return this.slotGroups.values();
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public @Nullable SyncHandler getSyncHandler(String mapKey) {
        return getSyncHandlerFromMapKey(mapKey);
    }

    public @Nullable SyncHandler getSyncHandlerFromMapKey(String mapKey) {
        return this.syncHandlers.get(mapKey);
    }

    @Override
    public @Nullable SyncHandler findSyncHandlerNullable(String name, int id) {
        return this.syncHandlers.get(makeSyncKey(name, id));
    }

    public Player getPlayer() {
        return getModularSyncManager().getPlayer();
    }

    public ISyncRegistrar<?> getHyperVisor() {
        return this.modularSyncManager.getMainPSM();
    }

    public ModularContainerMenu getContainer() {
        return getModularSyncManager().getMenu();
    }

    void allowTemporarySyncHandlerRegistration(boolean allow) {
        this.allowSyncHandlerRegistration = allow;
    }

    public static String makeSyncKey(String name, int id) {
        return name + ":" + id;
    }
}
