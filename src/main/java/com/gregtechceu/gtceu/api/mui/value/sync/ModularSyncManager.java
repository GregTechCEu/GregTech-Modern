package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;
import com.gregtechceu.gtceu.client.mui.screen.ClientScreenHandler;
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

import java.util.Map;
import java.util.Set;

public class ModularSyncManager {

    public static final String AUTO_SYNC_PREFIX = "auto_sync:";
    protected static final String PLAYER_INVENTORY = "player_inventory";
    private static final String CURSOR_KEY = makeSyncKey("cursor_slot", 255255);

    private final Map<String, PanelSyncManager> panelSyncManagerMap = new Object2ObjectOpenHashMap<>();
    // A set of all panels which have been opened during the ui. May also contain closed panels.
    // This is used to detect if
    private final Set<String> panelHistory = new ObjectOpenHashSet<>();
    @Getter
    private PanelSyncManager mainPSM;
    @Getter
    private final ModularContainerMenu menu;
    private final CursorSlotSyncHandler cursorSlotSyncHandler = new CursorSlotSyncHandler();

    public ModularSyncManager(ModularContainerMenu menu) {
        this.menu = menu;
    }

    @ApiStatus.Internal
    public void construct(String mainPanelName, PanelSyncManager mainPSM) {
        this.mainPSM = mainPSM;
        if (this.mainPSM.getSlotGroup(PLAYER_INVENTORY) == null) {
            this.mainPSM.bindPlayerInventory(getPlayer());
        }
        mainPSM.syncValue(CURSOR_KEY, this.cursorSlotSyncHandler);
        open(mainPanelName, mainPSM);
    }

    public void detectAndSendChanges(boolean init) {
        this.panelSyncManagerMap.values().forEach(psm -> psm.detectAndSendChanges(init));
    }

    public void onClose() {
        if (ClientScreenHandler.isGuiClosing()) {
            this.panelSyncManagerMap.values().forEach(PanelSyncManager::onClose);
        }
    }

    public void onOpen() {
        this.panelSyncManagerMap.values().forEach(PanelSyncManager::onOpen);
    }

    public void onUpdate() {
        this.panelSyncManagerMap.values().forEach(PanelSyncManager::onUpdate);
    }

    public PanelSyncManager getPanelSyncManager(String panelName) {
        PanelSyncManager psm = this.panelSyncManagerMap.get(panelName);
        if (psm != null) return psm;
        throw new NullPointerException("No PanelSyncManager found for name '" + panelName + "'!");
    }

    public SyncHandler getSyncHandler(String panelName, String syncKey) {
        return getPanelSyncManager(panelName).getSyncHandler(syncKey);
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

    public void open(String name, PanelSyncManager syncManager) {
        this.panelSyncManagerMap.put(name, syncManager);
        this.panelHistory.add(name);
        syncManager.initialize(name, this);
    }

    public void close(String name) {
        PanelSyncManager psm = this.panelSyncManagerMap.remove(name);
        if (psm != null) psm.onClose();
    }

    public boolean isOpen(String panelName) {
        return this.panelSyncManagerMap.containsKey(panelName);
    }

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

    public boolean isClient() {
        return this.menu.isClient();
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

    public static String makeSyncKey(String name, int id) {
        return name + ":" + id;
    }
}
