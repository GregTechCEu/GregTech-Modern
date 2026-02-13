package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.api.mui.base.IPanelHandler;
import com.gregtechceu.gtceu.api.mui.base.ISyncedAction;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.PlayerSlotGroup;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

public interface ISyncRegistrar<S extends ISyncRegistrar<S>> {

    boolean hasSyncHandler(SyncHandler syncHandler);

    default S syncValue(String name, SyncHandler syncHandler) {
        return syncValue(name, 0, syncHandler);
    }

    S syncValue(String name, int id, SyncHandler syncHandler);

    default S syncValue(int id, SyncHandler syncHandler) {
        return syncValue("_", id, syncHandler);
    }

    default S itemSlot(String key, ModularSlot slot) {
        return itemSlot(key, 0, slot);
    }

    default S itemSlot(String key, int id, ModularSlot slot) {
        return syncValue(key, id, new ItemSlotSyncHandler(slot));
    }

    default S itemSlot(int id, ModularSlot slot) {
        return itemSlot("_", id, slot);
    }

    default DynamicSyncHandler dynamicSyncHandler(String key, DynamicSyncHandler.IWidgetProvider widgetProvider) {
        return dynamicSyncHandler(key, 0, widgetProvider);
    }

    default DynamicSyncHandler dynamicSyncHandler(String key, int id,
                                                  DynamicSyncHandler.IWidgetProvider widgetProvider) {
        DynamicSyncHandler syncHandler = new DynamicSyncHandler().widgetProvider(widgetProvider);
        syncValue(key, id, syncHandler);
        return syncHandler;
    }

    IPanelHandler syncedPanel(String key, boolean subPanel, PanelSyncHandler.IPanelBuilder panelBuilder);

    @Nullable
    IPanelHandler findPanelHandlerNullable(String key);

    default @NotNull IPanelHandler findPanelHandler(String key) {
        IPanelHandler panelHandler = findPanelHandlerNullable(key);
        if (panelHandler == null) {
            throw new NoSuchElementException(
                    "Expected to find panel sync handler with key '" + key + "', but none was found.");
        }
        return panelHandler;
    }

    S registerSlotGroup(SlotGroup slotGroup);

    default S registerSlotGroup(String name, int rowSize, int shiftClickPriority) {
        return registerSlotGroup(new SlotGroup(name, rowSize, shiftClickPriority, true));
    }

    default S registerSlotGroup(String name, int rowSize, boolean allowShiftTransfer) {
        return registerSlotGroup(new SlotGroup(name, rowSize, 100, allowShiftTransfer));
    }

    default S registerSlotGroup(String name, int rowSize) {
        return registerSlotGroup(new SlotGroup(name, rowSize, 100, true));
    }

    default S bindPlayerInventory(Player player) {
        return bindPlayerInventory(player, ModularSlot::new);
    }

    default S bindPlayerInventory(Player player, @NotNull PanelSyncManager.SlotFunction slotFunction) {
        if (getSlotGroup(PlayerSlotGroup.NAME) != null) {
            throw new IllegalStateException("The player slot group is already registered!");
        }
        PlayerMainInvWrapper playerInventory = new PlayerMainInvWrapper(player.getInventory());
        String key = "player";
        for (int i = 0; i < 36; i++) {
            itemSlot(key, i, slotFunction.apply(playerInventory, i).slotGroup(PlayerSlotGroup.NAME));
        }
        // player inv sorting is handled by bogosorter
        registerSlotGroup(new PlayerSlotGroup(PlayerSlotGroup.NAME));
        return (S) this;
    }

    default S registerSyncedAction(String mapKey, ISyncedAction action) {
        return registerSyncedAction(mapKey, true, true, action);
    }

    default S registerSyncedAction(String mapKey, Side side, ISyncedAction action) {
        return registerSyncedAction(mapKey, side.isClient(), side.isServer(), action);
    }

    default S registerClientSyncedAction(String mapKey, ISyncedAction action) {
        return registerSyncedAction(mapKey, true, false, action);
    }

    default S registerServerSyncedAction(String mapKey, ISyncedAction action) {
        return registerSyncedAction(mapKey, false, true, action);
    }

    S registerSyncedAction(String mapKey, boolean executeClient, boolean executeServer, ISyncedAction action);

    default <T extends SyncHandler> T getOrCreateSyncHandler(String name, Class<T> clazz, Supplier<T> supplier) {
        return getOrCreateSyncHandler(name, 0, clazz, supplier);
    }

    <T extends SyncHandler> T getOrCreateSyncHandler(String name, int id, Class<T> clazz, Supplier<T> supplier);

    default ItemSlotSyncHandler getOrCreateSlot(String name, int id, Supplier<ModularSlot> slotSupplier) {
        return getOrCreateSyncHandler(name, id, ItemSlotSyncHandler.class,
                () -> new ItemSlotSyncHandler(slotSupplier.get()));
    }

    @Nullable
    SyncHandler findSyncHandlerNullable(String name, int id);

    default @Nullable SyncHandler findSyncHandlerNullable(String name) {
        return findSyncHandlerNullable(name, 0);
    }

    default @NotNull SyncHandler findSyncHandler(String name, int id) {
        SyncHandler syncHandler = findSyncHandlerNullable(name, id);
        if (syncHandler == null) {
            throw new NoSuchElementException(
                    "Expected to find sync handler with key '" + makeSyncKey(name, id) + "', but none was found.");
        }
        return syncHandler;
    }

    default @NotNull SyncHandler findSyncHandler(String name) {
        return findSyncHandler(name, 0);
    }

    default <T extends SyncHandler> @Nullable T findSyncHandlerNullable(String name, int id, Class<T> type) {
        SyncHandler syncHandler = findSyncHandlerNullable(name, id);
        if (syncHandler != null && type.isAssignableFrom(syncHandler.getClass())) {
            return type.cast(syncHandler);
        }
        return null;
    }

    default <T extends SyncHandler> @Nullable T findSyncHandlerNullable(String name, Class<T> type) {
        return findSyncHandlerNullable(name, 0, type);
    }

    default <T extends SyncHandler> @NotNull T findSyncHandler(String name, int id, Class<T> type) {
        SyncHandler syncHandler = findSyncHandlerNullable(name, id);
        if (syncHandler == null) {
            throw new NoSuchElementException(
                    "Expected to find sync handler with key '" + makeSyncKey(name, id) + "', but none was found.");
        }
        if (!type.isAssignableFrom(syncHandler.getClass())) {
            throw new ClassCastException("Expected to find sync handler with key '" + makeSyncKey(name, id) +
                    "' of type '" + type.getName() + "', but found type '" + syncHandler.getClass().getName() + "'.");
        }
        return type.cast(syncHandler);
    }

    default <T extends SyncHandler> @NotNull T findSyncHandler(String name, Class<T> type) {
        return findSyncHandler(name, 0, type);
    }

    SlotGroup getSlotGroup(String name);

    static String makeSyncKey(String name, int id) {
        return name + ":" + id;
    }
}
