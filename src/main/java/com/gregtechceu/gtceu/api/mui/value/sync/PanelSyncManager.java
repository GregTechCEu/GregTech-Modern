package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.IPanelHandler;
import com.gregtechceu.gtceu.api.mui.base.ISyncedAction;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;
import com.gregtechceu.gtceu.client.mui.screen.ModularContainerMenu;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.ui.SyncHandlerPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PanelSyncManager {

    private final Map<String, SyncHandler> syncHandlers = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<String, SlotGroup> slotGroups = new Object2ObjectOpenHashMap<>();
    private final Map<SyncHandler, String> reverseSyncHandlers = new Object2ObjectOpenHashMap<>();
    private final Map<String, ISyncedAction> syncedActions = new Object2ObjectOpenHashMap<>();
    private final Map<String, SyncHandler> subPanels = new Object2ObjectArrayMap<>();
    private ModularSyncManager modularSyncManager;
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

    public PanelSyncManager(boolean client) {
        this.client = client;
    }

    @ApiStatus.Internal
    public void initialize(String panelName, ModularSyncManager msm) {
        this.modularSyncManager = msm;
        this.panelName = panelName;
        this.syncHandlers.forEach((mapKey, syncHandler) -> syncHandler.init(mapKey, this));
        this.locked = true;
        this.init = true;
        this.subPanels.forEach((s, syncHandler) -> msm.getMainPSM().registerPanelSyncHandler(s, syncHandler));
    }

    private void registerPanelSyncHandler(String name, SyncHandler syncHandler) {
        // only called on main psm
        SyncHandler currentSh = this.syncHandlers.get(name);
        if (currentSh != null && currentSh != syncHandler) throw new IllegalStateException();
        String currentName = this.reverseSyncHandlers.get(syncHandler);
        if (currentName != null && !name.equals(currentName)) throw new IllegalStateException();
        this.syncHandlers.put(name, syncHandler);
        this.reverseSyncHandlers.put(syncHandler, name);
        syncHandler.init(name, this);
    }

    void closeSubPanels() {
        this.subPanels.values().forEach(syncHandler -> {
            if (syncHandler instanceof IPanelHandler panelHandler) {
                if (panelHandler.isSubPanel()) {
                    panelHandler.closePanel();
                }
            } else {
                throw new IllegalStateException();
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
        // previously panel sync handlers were removed from the main psm, however this problematic if the screen will be
        // reopened at some point.
        // we can just not remove the sync handlers since mui has proper checks for re-registering panels
    }

    public boolean isInitialised() {
        return this.modularSyncManager != null;
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
        ISyncedAction syncedAction = this.syncedActions.get(mapKey);
        if (syncedAction == null) {
            GTCEu.LOGGER.warn("SyncAction '{}' does not exist for panel '{}'!.", mapKey, panelName);
            return false;
        }
        allowTemporarySyncHandlerRegistration(true);
        syncedAction.invoke(buf);
        allowTemporarySyncHandlerRegistration(false);
        return true;
    }

    public ItemStack getCursorItem() {
        return getModularSyncManager().getCursorItem();
    }

    public void setCursorItem(ItemStack stack) {
        getModularSyncManager().setCursorItem(stack);
    }

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

    public PanelSyncManager syncValue(String name, SyncHandler syncHandler) {
        return syncValue(name, 0, syncHandler);
    }

    public PanelSyncManager syncValue(String name, int id, SyncHandler syncHandler) {
        Objects.requireNonNull(name, "Name must not be null");
        Objects.requireNonNull(syncHandler, "Sync Handler must not be null");
        putSyncValue(name, id, syncHandler);
        return this;
    }

    public PanelSyncManager syncValue(int id, SyncHandler syncHandler) {
        return syncValue("_", id, syncHandler);
    }

    public PanelSyncManager itemSlot(String key, ModularSlot slot) {
        return itemSlot(key, 0, slot);
    }

    public PanelSyncManager itemSlot(String key, int id, ModularSlot slot) {
        return syncValue(key, id, new ItemSlotSH(slot));
    }

    public PanelSyncManager itemSlot(int id, ModularSlot slot) {
        return itemSlot("_", id, slot);
    }

    public DynamicSyncHandler dynamicSyncHandler(String key, DynamicSyncHandler.IWidgetProvider widgetProvider) {
        return dynamicSyncHandler(key, 0, widgetProvider);
    }

    public DynamicSyncHandler dynamicSyncHandler(String key, int id,
                                                 DynamicSyncHandler.IWidgetProvider widgetProvider) {
        DynamicSyncHandler syncHandler = new DynamicSyncHandler().widgetProvider(widgetProvider);
        syncValue(key, id, syncHandler);
        return syncHandler;
    }

    /**
     * Creates a synced panel handler. This can be used to automatically handle syncing for synced panels.
     * Synced panels do not need to be synced themselves, but contain at least one widget which is synced.
     * <p>
     * NOTE
     * </p>
     * A panel sync handler is only created once. If one was already registered, that one will be returned.
     * (This is only relevant for nested sub panels.)
     *
     * @param key          the key used for syncing
     * @param panelBuilder the panel builder, that will create the new panel. It must not return null or any existing
     *                     panels.
     * @param subPanel     true if this panel should close when its parent closes (the parent is defined by <i>this</i>
     *                     {@link PanelSyncManager})
     * @return a synced panel handler.
     * @throws NullPointerException     if the build panel of the builder is null
     * @throws IllegalArgumentException if the build panel of the builder is the main panel
     */
    public IPanelHandler panel(String key, PanelSyncHandler.IPanelBuilder panelBuilder, boolean subPanel) {
        SyncHandler sh = this.subPanels.get(key);
        if (sh != null) return (IPanelHandler) sh;
        PanelSyncHandler syncHandler = new PanelSyncHandler(panelBuilder, subPanel);
        this.subPanels.put(key, syncHandler);
        return syncHandler;
    }

    public PanelSyncManager registerSlotGroup(SlotGroup slotGroup) {
        if (!slotGroup.isSingleton()) {
            this.slotGroups.put(slotGroup.getName(), slotGroup);
        }
        return this;
    }

    public PanelSyncManager registerSlotGroup(String name, int rowSize, int shiftClickPriority) {
        return registerSlotGroup(new SlotGroup(name, rowSize, shiftClickPriority, true));
    }

    public PanelSyncManager registerSlotGroup(String name, int rowSize, boolean allowShiftTransfer) {
        return registerSlotGroup(new SlotGroup(name, rowSize, 100, allowShiftTransfer));
    }

    public PanelSyncManager registerSlotGroup(String name, int rowSize) {
        return registerSlotGroup(new SlotGroup(name, rowSize, 100, true));
    }

    public PanelSyncManager bindPlayerInventory(Player player) {
        return bindPlayerInventory(player, ModularSlot::new);
    }

    public PanelSyncManager bindPlayerInventory(Player player, @NotNull SlotFunction slotFunction) {
        if (getSlotGroup(ModularSyncManager.PLAYER_INVENTORY) != null) {
            throw new IllegalStateException("The player slot group is already registered!");
        }
        PlayerMainInvWrapper playerInventory = new PlayerMainInvWrapper(player.getInventory());
        String key = "player";
        for (int i = 0; i < 36; i++) {
            itemSlot(key, i, slotFunction.apply(playerInventory, i).slotGroup(ModularSyncManager.PLAYER_INVENTORY));
        }
        // player inv sorting is handled by bogosorter
        registerSlotGroup(new SlotGroup(ModularSyncManager.PLAYER_INVENTORY, 9, SlotGroup.PLAYER_INVENTORY_PRIO, true)
                .setAllowSorting(false));
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

    public PanelSyncManager registerSyncedAction(String mapKey, ISyncedAction action) {
        this.syncedActions.put(mapKey, action);
        return this;
    }

    public void callSyncedAction(String mapKey, FriendlyByteBuf packet) {
        if (invokeSyncedAction(mapKey, packet)) {
            SyncHandlerPacket packetSyncHandler = new SyncHandlerPacket(this.panelName, mapKey, true, packet);
            if (isClient()) {
                GTNetwork.sendToServer(packetSyncHandler);
            } else {
                GTNetwork.sendToPlayer((ServerPlayer) getPlayer(), packetSyncHandler);
            }
        }
    }

    public void callSyncedAction(String mapKey, Consumer<FriendlyByteBuf> packetBuilder) {
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packetBuilder.accept(packet);
        callSyncedAction(mapKey, packet);
    }

    public <T extends SyncHandler> T getOrCreateSyncHandler(String name, Class<T> clazz, Supplier<T> supplier) {
        return getOrCreateSyncHandler(name, 0, clazz, supplier);
    }

    public <T extends SyncHandler> T getOrCreateSyncHandler(String name, int id, Class<T> clazz, Supplier<T> supplier) {
        SyncHandler syncHandler = getSyncHandler(name);
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
            return (T) syncHandler;
        }
        throw new IllegalStateException("SyncHandler for key " + makeSyncKey(name, id) + " is of type " +
                syncHandler.getClass() + ", but type " + clazz + " was expected!");
    }

    public ItemSlotSH getOrCreateSlot(String name, int id, Supplier<ModularSlot> slotSupplier) {
        return getOrCreateSyncHandler(name, id, ItemSlotSH.class, () -> new ItemSlotSH(slotSupplier.get()));
    }

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

    public @Nullable SyncHandler findSyncHandlerNullable(String name, int id) {
        return this.syncHandlers.get(makeSyncKey(name, id));
    }

    public @Nullable SyncHandler findSyncHandlerNullable(String name) {
        return findSyncHandlerNullable(name, 0);
    }

    public @NotNull SyncHandler findSyncHandler(String name, int id) {
        SyncHandler syncHandler = this.syncHandlers.get(makeSyncKey(name, id));
        if (syncHandler == null) {
            throw new NoSuchElementException(
                    "Expected to find sync handler with key '" + makeSyncKey(name, id) + "', but none was found.");
        }
        return syncHandler;
    }

    public @NotNull SyncHandler findSyncHandler(String name) {
        return findSyncHandler(name, 0);
    }

    @SuppressWarnings("unchecked")
    public <T extends SyncHandler> @Nullable T findSyncHandlerNullable(String name, int id, Class<T> type) {
        SyncHandler syncHandler = this.syncHandlers.get(makeSyncKey(name, id));
        if (syncHandler != null && type.isAssignableFrom(syncHandler.getClass())) {
            return (T) syncHandler;
        }
        return null;
    }

    public <T extends SyncHandler> @Nullable T findSyncHandlerNullable(String name, Class<T> type) {
        return findSyncHandlerNullable(name, 0, type);
    }

    @SuppressWarnings("unchecked")
    public <T extends SyncHandler> @NotNull T findSyncHandler(String name, int id, Class<T> type) {
        SyncHandler syncHandler = this.syncHandlers.get(makeSyncKey(name, id));
        if (syncHandler == null) {
            throw new NoSuchElementException(
                    "Expected to find sync handler with key '" + makeSyncKey(name, id) + "', but none was found.");
        }
        if (!type.isAssignableFrom(syncHandler.getClass())) {
            throw new ClassCastException("Expected to find sync handler with key '" + makeSyncKey(name, id) +
                    "' of type '" + type.getName() + "', but found type '" + syncHandler.getClass().getName() + "'.");
        }
        return (T) syncHandler;
    }

    public <T extends SyncHandler> @NotNull T findSyncHandler(String name, Class<T> type) {
        return findSyncHandler(name, 0, type);
    }

    public Player getPlayer() {
        return getModularSyncManager().getPlayer();
    }

    public ModularSyncManager getModularSyncManager() {
        if (!isInitialised()) {
            throw new IllegalStateException("PanelSyncManager is not yet initialised!");
        }
        return modularSyncManager;
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
