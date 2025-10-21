package com.gregtechceu.gtceu.common.data.mui;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.drawable.text.StringKey;
import com.gregtechceu.gtceu.api.mui.factory.PanelFactory;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.TextWidget;
import com.gregtechceu.gtceu.api.mui.widgets.slot.FluidSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.function.IntFunction;

public class GTMuiRecipeTypePanel {

    public static PanelFactory RECIPE_TYPE = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                             MetaMachine machine) -> {

        if (!(machine instanceof WorkableTieredMachine workableMachine)) return null;

        var recipeType = workableMachine.getRecipeType();

        ModularPanel panel = new ModularPanel(machine.getDefinition().getName());

        panel.child(IKey.str(machine.getDefinition().getName()).asWidget().left(4));

        Table<RecipeCapability<?>, IO, SlotGroup> slotGroups = HashBasedTable.create();

        int x = 10;
        int y = 20;
        for (var entry : recipeType.maxInputs.object2IntEntrySet()) {
            var recipeCap = entry.getKey();
            int max = entry.getIntValue();
            SlotGroup group = new SlotGroup(recipeCap.name + "_in", max);
            slotGroups.put(recipeCap, IO.IN, group);
            syncManager.registerSlotGroup(group);

            int side = (int)Math.ceil(Math.sqrt(max));
            String[] matrix = new String[side];
            for (int i = 0; i < side; i++) {
                StringBuilder s = new StringBuilder();
                for (int j = 0; j < side; j++) {
                    if (i * side + j < max) {
                        s.append("S");
                    }
                }
                matrix[i] = s.toString();
            }

            IntFunction<IWidget> widget;
            if (recipeCap == ItemRecipeCapability.CAP) {
                NotifiableItemStackHandler itemHandler = workableMachine.importItems;
                widget = i -> new ItemSlot().slot(new ModularSlot(itemHandler, i));
            } else if (recipeCap == FluidRecipeCapability.CAP) {
                NotifiableFluidTank fluidHandler = workableMachine.importFluids;
                widget = i -> new FluidSlot().syncHandler(fluidHandler.getStorages()[i]);
            } else {
                widget = null;
            }

            if (widget != null) {
                panel.child(SlotGroupWidget.builder()
                        .matrix(matrix)
                        .key('S', widget)
                        .build().left(x).top(y));
            }

            y += 18 * side;
        }

        x = 60;
        y = 20;

        for (var entry : recipeType.maxOutputs.object2IntEntrySet()) {
            var recipeCap = entry.getKey();
            int max = entry.getIntValue();
            SlotGroup group = new SlotGroup(recipeCap.name + "_out", max);
            slotGroups.put(recipeCap, IO.OUT, group);
            syncManager.registerSlotGroup(group);

            int side = (int)Math.ceil(Math.sqrt(max));
            String[] matrix = new String[side];
            for (int i = 0; i < side; i++) {
                StringBuilder s = new StringBuilder();
                for (int j = 0; j < side; j++) {
                    if (i * side + j < max) {
                        s.append("S");
                    }
                }
                matrix[i] = s.toString();
            }

            IntFunction<IWidget> widget;
            if (recipeCap == ItemRecipeCapability.CAP) {
                NotifiableItemStackHandler itemHandler = workableMachine.exportItems;
                widget = i -> new ItemSlot().slot(new ModularSlot(itemHandler, i));
            } else if (recipeCap == FluidRecipeCapability.CAP) {
                NotifiableFluidTank fluidHandler = workableMachine.exportFluids;
                widget = i -> new FluidSlot().syncHandler(fluidHandler.getStorages()[i]);
            } else {
                widget = null;
            }

            if (widget != null) {
                panel.child(SlotGroupWidget.builder()
                        .matrix(matrix)
                        .key('S', widget)
                        .build()
                        .left(x).top(y));
            }

            y += 18 * side;
        }

        panel.bindPlayerInventory();
        return panel;
    };
}
