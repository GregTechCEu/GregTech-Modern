package com.gregtechceu.gtceu.common.data.mui;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.factory.PanelEditor;
import com.gregtechceu.gtceu.api.mui.factory.PanelFactory;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.utils.WidgetUtil;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ProgressWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.slot.FluidSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;

public class GTMuiRecipeTypePanel {

    public static PanelEditor RECIPE_SLOTS = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                              MetaMachine machine, ModularPanel panel) -> {

        if (!(machine instanceof WorkableTieredMachine workableMachine)) {
            GTCEu.LOGGER.error("{} is not a WorkableTieredMachine, can not add slots to its content",
                    machine.getDefinition().getName());
            return;
        }

        var leftColumn = WidgetUtil.getWidget(panel, "left column");
        var rightColumn = WidgetUtil.getWidget(panel, "right column");
        if(!(leftColumn instanceof Column input) || !(rightColumn instanceof Column output)) {
            return;
        }


        var recipeType = workableMachine.getRecipeType();
        Table<RecipeCapability<?>, IO, SlotGroup> slotGroups = HashBasedTable.create();



        int x = 10;
        int y = 20;
        for (var entry : recipeType.maxInputs.object2IntEntrySet()) {
            var recipeCap = entry.getKey();
            int max = entry.getIntValue();
            SlotGroup group = new SlotGroup(recipeCap.name + "_in", max);
            slotGroups.put(recipeCap, IO.IN, group);
            syncManager.registerSlotGroup(group);

            int side = (int) Math.ceil(Math.sqrt(max));
            int row = max > 3 ? side : 1;
            String[] matrix = new String[row];
            for (int i = 0; i < row; i++) {
                StringBuilder s = new StringBuilder();
                for (int j = 0; j <side; j++) {
                    if (i * side + j < max) {
                        s.append("S");
                    }
                }
                matrix[i] = s.toString();
            }

            IntFunction<IWidget> slotWidget;
            if (recipeCap == ItemRecipeCapability.CAP) {
                NotifiableItemStackHandler itemHandler = workableMachine.importItems;
                slotWidget = i -> new ItemSlot().slot(new ModularSlot(itemHandler, i));
            } else if (recipeCap == FluidRecipeCapability.CAP) {
                NotifiableFluidTank fluidHandler = workableMachine.importFluids;
                slotWidget = i -> new FluidSlot().syncHandler(fluidHandler.getStorages()[i]);
            } else {
                slotWidget = null;
            }

            if (slotWidget != null) {

                input.child(SlotGroupWidget.builder()
                                .matrix(matrix)
                                .key('S', slotWidget)
                                .build().name("inputs")
                        .align(Alignment.CENTER));

            }

            y += 18 * side;
        }
        for (var entry : recipeType.maxOutputs.object2IntEntrySet()) {
            var recipeCap = entry.getKey();
            int max = entry.getIntValue();
            SlotGroup group = new SlotGroup(recipeCap.name + "_out", max);
            slotGroups.put(recipeCap, IO.OUT, group);
            syncManager.registerSlotGroup(group);

            int side = (int) Math.ceil(Math.sqrt(max));
            int row = max > 3 ? side : 1;
            String[] matrix = new String[row];
            for (int i = 0; i < row; i++) {
                StringBuilder s = new StringBuilder();
                for (int j = 0; j < side; j++) {
                    if (i * side + j < max) {
                        s.append("S");
                    }
                }
                matrix[i] = s.toString();
            }

            IntFunction<IWidget> slotWidget;
            if (recipeCap == ItemRecipeCapability.CAP) {
                NotifiableItemStackHandler itemHandler = workableMachine.exportItems;
                slotWidget = i -> new ItemSlot().slot(new ModularSlot(itemHandler, i));
            } else if (recipeCap == FluidRecipeCapability.CAP) {
                NotifiableFluidTank fluidHandler = workableMachine.exportFluids;
                slotWidget = i -> new FluidSlot().syncHandler(fluidHandler.getStorages()[i]);
            } else {
                slotWidget = null;
            }

            if (slotWidget != null) {
                output.child(SlotGroupWidget.builder()
                                .matrix(matrix)
                                .key('S', slotWidget)
                                .build().align(Alignment.CENTER));
            }
        }


    };


    public static PanelFactory RECIPE_TYPE = GTMuiPanels.BASE_PANEL.andThen(RECIPE_SLOTS, GTMuiEditors.CHARGE_SLOT,
            GTMuiEditors.PROGRESS_BAR(GTGuiTextures.PROGRESS_BAR_ARROW, 30, ProgressWidget.Direction.RIGHT), GTMuiEditors.POWER_BUTTON);
}
