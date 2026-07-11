package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.machine.trait.AutoOutputTrait;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.slot.FluidSlot;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.slot.SlotGroup;
import lombok.Getter;

import java.util.List;

public class BufferMachine extends TieredMachine implements IMuiMachine {

    public static final int TANK_SIZE = 64000;

    @SaveField
    @Getter
    protected final NotifiableItemStackHandler inventory;

    @SaveField
    @Getter
    protected final NotifiableFluidTank tank;
    @SaveField
    @SyncToClient

    public final AutoOutputTrait autoOutput;

    public BufferMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
        this.inventory = attachTrait(new NotifiableItemStackHandler(getInventorySize(tier), IO.BOTH));
        this.tank = attachTrait(new NotifiableFluidTank(getTankSize(tier), TANK_SIZE, IO.BOTH));
        this.autoOutput = attachTrait(new AutoOutputTrait(List.of(inventory), List.of(tank)));
    }

    ////////////////////////////////
    // ***** Initialization ******//
    ////////////////////////////////

    public static int getInventorySize(int tier) {
        return (int) Math.pow(tier + 2, 2);
    }

    public static int getTankSize(int tier) {
        return tier + 2;
    }

    ////////////////////////////////
    // ********** GUI *********** //
    ////////////////////////////////

    // TODO MUI: Needs EIO widget
    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        for (int i = 0; i < tank.getTanks(); i++) {
            syncManager.syncValue("fluids", i, SyncHandlers.fluidSlot(tank.getStorages()[i]));
        }

        SlotGroup slotGroup = new SlotGroup("inventory", inventory.getSlots());

        int size = tank.getTanks();
        String[] matrix = new String[size];
        for (int i = 0; i < size; i++) {
            String row = "I".repeat(size) + "F";
            matrix[i] = row;
        }

        SlotGroupWidget slotWidget = SlotGroupWidget.builder()
                .matrix(matrix)
                .key('I', i -> new ItemSlot()
                        .slot(new ModularSlot(inventory, i)
                                .slotGroup(slotGroup)))
                .key('F', i -> new FluidSlot()
                        .syncHandler("fluids", i))
                .build();

        mainWidget.child(slotWidget.center().margin(0, 10));
    }
}
