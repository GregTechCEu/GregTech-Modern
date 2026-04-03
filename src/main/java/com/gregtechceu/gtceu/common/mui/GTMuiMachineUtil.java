package com.gregtechceu.gtceu.common.mui;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;

import net.minecraftforge.items.IItemHandler;

import brachy.modularui.value.sync.ItemSlotSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.slot.FluidSlot;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.slot.SlotGroup;

import java.util.function.UnaryOperator;

public class GTMuiMachineUtil {

    public static SlotGroupWidget createSlotGroupFromInventory(IItemHandler itemHandler, String slotGroupName,
                                                               int maxSlots, char key, PanelSyncManager syncManager,
                                                               String... matrix) {
        return createSlotGroupFromInventory(itemHandler, slotGroupName, maxSlots, key, i -> i, syncManager, matrix);
    }

    public static SlotGroupWidget createSlotGroupFromInventory(IItemHandler itemHandler,
                                                               String slotGroupName, int maxSlots, char key,
                                                               UnaryOperator<ItemSlot> slotModifier,
                                                               PanelSyncManager syncManager,
                                                               String... matrix) {
        SlotGroup slotGroup = new SlotGroup(slotGroupName, maxSlots);

        return SlotGroupWidget.builder()
                .matrix(matrix)
                .key(key, i -> {
                    ModularSlot slot = new ModularSlot(itemHandler, i);
                    ItemSlotSyncHandler syncHandler = new ItemSlotSyncHandler(slot.slotGroup(slotGroup));
                    syncManager.syncValue(slotGroupName, i, syncHandler);
                    return slotModifier.apply(new ItemSlot()
                            .syncHandler(slotGroupName, i));
                })
                .build();
    }

    public static String[] createSquareMatrix(int size, char key) {
        size = (int) Math.sqrt(size);
        String[] matrix = new String[size];
        for (int i = 0; i < size; i++) {
            matrix[i] = String.valueOf(key).repeat(size);
        }
        return matrix;
    }

    public static SlotGroupWidget createSquareSlotGroupFromInventory(IItemHandler itemHandler,
                                                                     String slotGroupName,
                                                                     PanelSyncManager syncManager) {
        return createSlotGroupFromInventory(itemHandler, slotGroupName, itemHandler.getSlots(), 'I', syncManager,
                createSquareMatrix(itemHandler.getSlots(), 'I'));
    }

    public static SlotGroupWidget createSlotGroupFromInventory(PanelSyncManager syncManager,
                                                               NotifiableFluidTank fluidTank,
                                                               String syncHandlerName, int maxSlots, char key,
                                                               String... matrix) {
        for (int i = 0; i < maxSlots; i++) {
            syncManager.syncValue(syncHandlerName, i, SyncHandlers.fluidSlot(fluidTank.getStorages()[i]));
        }

        return SlotGroupWidget.builder()
                .matrix(matrix)
                .key(key, i -> new FluidSlot()
                        .syncHandler(syncHandlerName, i))
                .build();
    }
}
