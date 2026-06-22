package com.gregtechceu.gtceu.api.recipe.gui;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;

import brachy.modularui.value.sync.FluidSlotSyncHandler;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.slot.FluidSlot;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.slot.SlotGroup;

/**
 * Builds and attaches the UI for a specific capability in a singleblock recipe machine UI.
 */
@FunctionalInterface
public interface MachineCapabilityLayoutBuilder {

    /**
     * Builds and attaches the UI for a specific capability in a singleblock recipe machine UI.
     *
     * @param machine The singleblock machine, will be either a {@link SimpleTieredMachine} or
     *                {@link SimpleSteamMachine}.
     * @param layout  The {@link GTRecipeTypeUILayout} which holds UI layout data.
     * @param widget  The recipe type widget. Generally, UIs should be attached to either
     *                {@link GTRecipeTypeMachineWidget#inputColumn} or {@link GTRecipeTypeMachineWidget#outputColumn}.
     * @param io      The IO mode widgets are being created for.
     */
    void createCapabilityUILayout(MetaMachine machine, GTRecipeTypeUILayout layout, GTRecipeTypeMachineWidget widget,
                                  IO io);

    /**
     * The default UI layout for item slots.
     */
    MachineCapabilityLayoutBuilder ITEM = (machine, layout, widget, io) -> {

        var handlers = ItemRecipeCapability.CAP.getCapabilityHandlers(machine, io);
        if (handlers.isEmpty()) return;
        NotifiableItemStackHandler itemHandler = handlers.get(0);
        if (itemHandler == null || layout.getRecipeType().getMaxSlots(ItemRecipeCapability.CAP, io) == 0) return;

        var slotGroup = new SlotGroup(ItemRecipeCapability.CAP.name + "_" + io.name(), 3);

        if (layout.getRecipeType().getMaxSlots(ItemRecipeCapability.CAP, io) == 1) {
            var slot = new ItemSlot()
                    .slot(new ModularSlot(itemHandler, 0)
                            .slotGroup(slotGroup)
                            .accessibility(io == IO.IN, true))
                    .backgroundOverlay(layout.capabilityInfo(ItemRecipeCapability.CAP).getOverlay(io, 0));
            if (io == IO.IN) widget.inputColumn.child(slot);
            else widget.outputColumn.child(slot);
            return;
        }

        var slotGroupWidget = SlotGroupWidget
                .builder()
                .matrix(layout.capabilityInfo(ItemRecipeCapability.CAP).getMachineGrid(io, machine))
                .key('s', i -> new ItemSlot()
                        .slot(new ModularSlot(itemHandler, i)
                                .slotGroup(slotGroup)
                                .accessibility(io == IO.IN, true))
                        .backgroundOverlay(layout.capabilityInfo(ItemRecipeCapability.CAP).getOverlay(io, i)))
                .build()
                .size(18, 18)
                .coverChildren(18, 18);

        if (io == IO.IN) widget.inputColumn.child(slotGroupWidget);
        else widget.outputColumn.child(slotGroupWidget);
    };

    /**
     * The default UI layout for fluid slots.
     */
    MachineCapabilityLayoutBuilder FLUID = (machine, layout, widget, io) -> {

        var handlers = FluidRecipeCapability.CAP.getCapabilityHandlers(machine, io);
        if (handlers.isEmpty()) return;
        NotifiableFluidTank fluidTank = handlers.get(0);
        if (fluidTank == null || layout.getRecipeType().getMaxSlots(FluidRecipeCapability.CAP, io) == 0) return;

        if (layout.getRecipeType().getMaxSlots(FluidRecipeCapability.CAP, io) == 1) {
            var slot = new FluidSlot()
                    .syncHandler(new FluidSlotSyncHandler(fluidTank.getStorages()[0]).controlsAmount(false))
                    .backgroundOverlay(layout.capabilityInfo(FluidRecipeCapability.CAP).getOverlay(io, 0));
            if (io == IO.IN) widget.inputColumn.child(slot);
            else widget.outputColumn.child(slot);
            return;
        }

        var slotGroupWidget = SlotGroupWidget.builder()
                .matrix(layout.capabilityInfo(FluidRecipeCapability.CAP).getMachineGrid(io, machine))
                .key('s', i -> new FluidSlot()
                        .syncHandler(new FluidSlotSyncHandler(fluidTank.getStorages()[0]).controlsAmount(true))
                        .backgroundOverlay(layout.capabilityInfo(FluidRecipeCapability.CAP).getOverlay(io, i)))
                .build()
                .size(18, 18)
                .coverChildren();

        if (io == IO.IN) widget.inputColumn.child(slotGroupWidget);
        else widget.outputColumn.child(slotGroupWidget);
    };
}
