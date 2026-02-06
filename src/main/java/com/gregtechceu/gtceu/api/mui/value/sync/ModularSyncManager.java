package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.IPanelHandler;
import com.gregtechceu.gtceu.api.mui.base.ISyncedAction;
import com.gregtechceu.gtceu.api.mui.widgets.slot.PlayerSlotGroup;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;
import com.gregtechceu.gtceu.client.mui.screen.ModularContainerMenu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ModularSyncManager implements ISyncRegistrar<ModularSyncManager> {

    public static final String AUTO_SYNC_PREFIX = "auto_sync:";
    private static final String CURSOR_KEY = ISyncRegistrar.makeSyncKey("cursor_slot", 255255);

    private final Map<String, PanelSyncManager> panelSyncManagerMap = new Object2ObjectOpenHashMap<>();
    // A set of all panels which have been opened during the ui. May also contain closed panels.
    // This is used to detect if
    private final Set<String> panelHistory = new ObjectOpenHashSet<>();
    @Getter
    private PanelSyncManager mainPSM;
    @Getter
    private ModularContainerMenu menu;
    private final CursorSlotSyncHandler cursorSlotSyncHandler = new CursorSlotSyncHandler();
    @Getter
    private final boolean client;
    private State state = State.INIT;

    public ModularSyncManager(boolean client) {
        this.client = client;
    }

    void setMainPSM(PanelSyncManager mainPSM) {
        this.mainPSM = mainPSM;
    }

    @ApiStatus.Internal
    public void construct(ModularContainerMenu menu, String mainPanelName) {
        this.menu = menu;
        if (this.mainPSM.getSlotGroup(PlayerSlotGroup.NAME) == null) {
            this.mainPSM.bindPlayerInventory(getPlayer());
        }
        this.mainPSM.syncValue(CURSOR_KEY, this.cursorSlotSyncHandler);
        open(mainPanelName, this.mainPSM);
    }

    @ApiStatus.Internal
    public void detectAndSendChanges(boolean init) {
        this.panelSyncManagerMap.values().forEach(psm -> psm.detectAndSendChanges(init));
    }

    @ApiStatus.Internal
    public void dispose() {
        if (isDisposed()) return;
        if (!isClosed()) throw new IllegalStateException("Sync manager must be closed before disposing!");
        this.panelSyncManagerMap.values().forEach(PanelSyncManager::onClose);
        this.panelSyncManagerMap.clear();
        this.menu.disposed();
        setState(State.DISPOSED);
    }

    @ApiStatus.Internal
    public void onOpen() {
        if (isOpen()) return;
        if (isDisposed()) throw new IllegalStateException("Can't open sync manager after it has been disposed!");
        if (this.menu == null) {
            throw new IllegalStateException(
                    "Sync Manager can't be opened when its not yet constructed. ModularContainer is null.");
        }
        setState(State.OPEN);
        this.panelSyncManagerMap.values().forEach(PanelSyncManager::onOpen);
        this.menu.opened();
    }

    @ApiStatus.Internal
    public void onClose() {
        if (!isOpen()) throw new IllegalStateException();
        this.menu.closed();
        setState(State.CLOSED);
    }

    public void onUpdate() {
        this.panelSyncManagerMap.values().forEach(PanelSyncManager::onUpdate);
    }

    public PanelSyncManager getPanelSyncManager(String panelName) {
        PanelSyncManager psm = this.panelSyncManagerMap.get(panelName);
        if (psm != null) return psm;
        throw new NullPointerException("No PanelSyncManager found for name '" + panelName + "'!");
    }

    @Nullable
    public SyncHandler getSyncHandler(String panelName, String syncKey) {
        return getPanelSyncManager(panelName).getSyncHandlerFromMapKey(syncKey);
    }

    public SlotGroup getSlotGroup(String panelName, String slotGroupName) {
        return getPanelSyncManager(panelName).getSlotGroup(slotGroupName);
    }

    public ItemStack getCursorItem() {
        return getPlayer().containerMenu.getCarried();
    }

    public void setCursorItem(ItemStack item) {
        getPlayer().containerMenu.setCarried(item);
        this.cursorSlotSyncHandler.sync();
    }

    @ApiStatus.Internal
    public void open(String name, PanelSyncManager syncManager) {
        this.panelSyncManagerMap.put(name, syncManager);
        this.panelHistory.add(name);
        syncManager.initialize(name);
    }

    @ApiStatus.Internal
    public void close(String name) {
        PanelSyncManager psm = this.panelSyncManagerMap.remove(name);
        if (psm != null) psm.onClose();
    }

    public boolean isOpen(String panelName) {
        return this.panelSyncManagerMap.containsKey(panelName);
    }

    @ApiStatus.Internal
    public void receiveWidgetUpdate(String panelName, String mapKey, boolean action, int id, FriendlyByteBuf buf) {
        PanelSyncManager psm = this.panelSyncManagerMap.get(panelName);
        if (psm != null) {
            psm.receiveWidgetUpdate(mapKey, action, id, buf);
        } else if (!this.panelHistory.contains(panelName)) {
            GTCEu.LOGGER.throwing(new IllegalStateException(
                    "A packet was send to panel '\" + panelName + \"' which was not opened yet!"));
        }
        // else the panel was open at some point
        // we simply discard the packet silently and assume the packet was correctly send, but the panel closed earlier
    }

    public Player getPlayer() {
        return this.menu.getPlayer();
    }

    private static boolean isPlayerSlot(Slot slot) {
        if (slot == null) return false;
        if (slot.container instanceof Inventory) {
            return slot.getSlotIndex() >= 0 && slot.getSlotIndex() < 36;
        }
        if (slot instanceof SlotItemHandler slotItemHandler) {
            IItemHandler iItemHandler = slotItemHandler.getItemHandler();
            if (iItemHandler instanceof PlayerMainInvWrapper || iItemHandler instanceof PlayerInvWrapper) {
                return slot.getSlotIndex() >= 0 && slot.getSlotIndex() < 36;
            }
        }
        return false;
    }

    @Override
    public boolean hasSyncHandler(SyncHandler syncHandler) {
        return this.mainPSM.hasSyncHandler(syncHandler);
    }

    @Override
    public ModularSyncManager syncValue(String name, int id, SyncHandler syncHandler) {
        this.mainPSM.syncValue(name, id, syncHandler);
        return this;
    }

    @Override
    public IPanelHandler syncedPanel(String key, boolean subPanel, PanelSyncHandler.IPanelBuilder panelBuilder) {
        return this.mainPSM.syncedPanel(key, subPanel, panelBuilder);
    }

    @Override
    public @Nullable IPanelHandler findPanelHandlerNullable(String key) {
        return this.mainPSM.findPanelHandlerNullable(key);
    }

    @Override
    public ModularSyncManager registerSlotGroup(SlotGroup slotGroup) {
        this.mainPSM.registerSlotGroup(slotGroup);
        return this;
    }

    @Override
    public ModularSyncManager registerSyncedAction(String mapKey, boolean executeClient, boolean executeServer,
                                                   ISyncedAction action) {
        this.mainPSM.registerSyncedAction(mapKey, executeClient, executeServer, action);
        return this;
    }

    @Override
    public <T extends SyncHandler> T getOrCreateSyncHandler(String name, int id, Class<T> clazz, Supplier<T> supplier) {
        return this.mainPSM.getOrCreateSyncHandler(name, id, clazz, supplier);
    }

    @Override
    public @Nullable SyncHandler findSyncHandlerNullable(String name, int id) {
        return this.mainPSM.findSyncHandlerNullable(name, id);
    }

    @Override
    public SlotGroup getSlotGroup(String name) {
        return this.mainPSM.getSlotGroup(name);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public static String makeSyncKey(String name, int id) {
        return ISyncRegistrar.makeSyncKey(name, id);
    }

    public boolean isOpen() {
        return this.state == State.OPEN;
    }

    public boolean isClosed() {
        return this.state == State.CLOSED || this.state == State.DISPOSED;
    }

    public boolean isDisposed() {
        return this.state == State.DISPOSED;
    }

    private void setState(State state) {
        this.state = state;
    }

    enum State {
        INIT,
        OPEN,
        CLOSED,
        DISPOSED;
    }
}
