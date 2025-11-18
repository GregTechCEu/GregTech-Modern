package com.gregtechceu.gtceu.common.data.mui;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.slot.FluidSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;

public class GTMuiMachineUtil {

    public static SlotGroupWidget createSlotGroupFromInventory(NotifiableItemStackHandler itemHandler,
                                                               String slotGroupName, int maxSlots, char key,
                                                               String... matrix) {
        SlotGroup slotGroup = new SlotGroup(slotGroupName, maxSlots);
        SlotGroupWidget slotWidget = SlotGroupWidget.builder()
                .matrix(matrix)
                .key(key, i -> new ItemSlot()
                        .slot(new ModularSlot(itemHandler, i)
                                .slotGroup(slotGroup)))
                .build();

        return slotWidget;
    }

    public static SlotGroupWidget createSlotGroupFromInventory(PanelSyncManager syncManager,
                                                               NotifiableFluidTank fluidTank,
                                                               String syncHandlerName, int maxSlots, char key,
                                                               String... matrix) {
        for (int i = 0; i < maxSlots; i++) {
            syncManager.syncValue(syncHandlerName, i, SyncHandlers.fluidSlot(fluidTank.getStorages()[i]));
        }

        SlotGroupWidget slotWidget = SlotGroupWidget.builder()
                .matrix(matrix)
                .key(key, i -> new FluidSlot()
                        .syncHandler(syncHandlerName, i))
                .build();

        return slotWidget;
    }
}
