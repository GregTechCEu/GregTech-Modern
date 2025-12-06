package com.gregtechceu.gtceu.common.data.mui;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.slot.FluidSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;

import net.minecraftforge.items.IItemHandler;

import java.util.function.UnaryOperator;

public class GTMuiMachineUtil {

    public static SlotGroupWidget createSlotGroupFromInventory(IItemHandler itemHandler, String slotGroupName,
                                                               int maxSlots, char key, String... matrix) {
        return createSlotGroupFromInventory(itemHandler, slotGroupName, maxSlots, key, i -> i, matrix);
    }

    public static SlotGroupWidget createSlotGroupFromInventory(IItemHandler itemHandler,
                                                               String slotGroupName, int maxSlots, char key,
                                                               UnaryOperator<ItemSlot> slotModifier,
                                                               String... matrix) {
        SlotGroup slotGroup = new SlotGroup(slotGroupName, maxSlots);

        return SlotGroupWidget.builder()
                .matrix(matrix)
                .key(key, i -> slotModifier.apply(new ItemSlot()
                        .slot(new ModularSlot(itemHandler, i)
                                .slotGroup(slotGroup))))
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
                                                                     String slotGroupName) {
        return createSlotGroupFromMatrix(slotGroupName, itemHandler.getSlots(), 'I',
                createSquareMatrix(itemHandler.getSlots(), 'I'));
    }

    public static SlotGroupWidget createSlotGroupFromMatrix(String slotGroupName, int maxSlots, char key,
                                                            String... matrix) {
        return SlotGroupWidget.builder()
                .matrix(matrix)
                .key(key, i -> new ItemSlot())
                .build();
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
