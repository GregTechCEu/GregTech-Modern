package com.gregtechceu.gtceu.common.data.mui;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.mui.value.sync.ItemSlotSyncHandler;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.slot.FluidSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;

import net.minecraftforge.items.IItemHandler;

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

    public static Flow createColumn(boolean reverse, Widget<?>... children) {
        var column = new Column();
        column.reverseLayout(reverse);
        for (Widget<?> child : children) {
            column.child(child);
        }
        return column;
    }
}
