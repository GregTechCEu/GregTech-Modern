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
import com.gregtechceu.gtceu.api.mui.widget.WidgetTree;
import com.gregtechceu.gtceu.api.mui.widgets.ProgressWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.api.mui.widgets.slot.FluidSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import java.util.*;
import java.util.function.IntFunction;

public class GTMuiRecipeTypePanel {

    public static PanelEditor RECIPE_SLOTS = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                              MetaMachine machine, ModularPanel panel) -> {

        if (!(machine instanceof WorkableTieredMachine workableMachine)) {
            GTCEu.LOGGER.error("{} is not a WorkableTieredMachine, can not add slots to its content",
                    machine.getDefinition().getName());
            return;
        }

        var input = WidgetTree.findFirstWithNameNullable(panel, "inputs", Column.class);
        var output = WidgetTree.findFirstWithNameNullable(panel, "outputs", Column.class);

        if(input == null || output == null) {
            return;
        }


        var recipeType = workableMachine.getRecipeType();
        Table<RecipeCapability<?>, IO, SlotGroup> slotGroups = HashBasedTable.create();

        Map<Character, IntFunction<IWidget>> inputSlotWidgets = new HashMap<>();
        List<String> inMatrix = new ArrayList<>();
        for (var entry : recipeType.maxInputs.object2IntEntrySet()) {
            var recipeCap = entry.getKey();
            int max = entry.getIntValue();
            if (max == 0) continue;

            SlotGroup group = new SlotGroup(recipeCap.name + "_in", max);
            slotGroups.put(recipeCap, IO.IN, group);
            syncManager.registerSlotGroup(group);

            IntFunction<IWidget> slotWidget;
            char cap = 0;
            if (recipeCap == ItemRecipeCapability.CAP) {
                NotifiableItemStackHandler itemHandler = workableMachine.importItems;
                slotWidget = i -> new ItemSlot().slot(new ModularSlot(itemHandler, i));
                cap = 'i';
            } else if (recipeCap == FluidRecipeCapability.CAP) {
                NotifiableFluidTank fluidHandler = workableMachine.importFluids;
                slotWidget = i -> new FluidSlot().syncHandler(fluidHandler.getStorages()[i]);
                cap = 'f';
            } else {
                continue;
            }

            int rows = calculateRowSize(max);
            int cols = max / rows;
            for (int i = 0; i < rows; i++) {
                StringBuilder s = new StringBuilder();
                for (int j = 0; j < cols; j++) {
                    if ((i * cols + j) >= max) {
                        s.append(" ");
                    } else {
                        s.append(cap);
                    }
                }
                inMatrix.add(s.toString());
            }

            inputSlotWidgets.put(cap, slotWidget);
        }

        /*
             II
              I
            FFF
            FFF
         */


        if (!inputSlotWidgets.isEmpty()) {

            var slotBuilder = SlotGroupWidget.builder()
                    .matrix(inMatrix.toArray(new String[0]));

            for (var entry : inputSlotWidgets.entrySet()) {
                slotBuilder.key(entry.getKey(), entry.getValue());
            }
            var slotInputWidget = slotBuilder.build();

            input.child(slotInputWidget.align(Alignment.CenterRight));
        }


        ////////////////////////////////
        Map<Character, IntFunction<IWidget>> outputSlotWidgets = new HashMap<>();
        List<String> outMatrix = new ArrayList<>();
        for (var entry : recipeType.maxOutputs.object2IntEntrySet()) {
            var recipeCap = entry.getKey();
            int max = entry.getIntValue();
            if (max == 0) continue;

            SlotGroup group = new SlotGroup(recipeCap.name + "_out", max);
            slotGroups.put(recipeCap, IO.OUT, group);
            syncManager.registerSlotGroup(group);

            IntFunction<IWidget> slotWidget;
            char cap = 0;
            if (recipeCap == ItemRecipeCapability.CAP) {
                NotifiableItemStackHandler itemHandler = workableMachine.exportItems;
                slotWidget = i -> new ItemSlot().slot(new ModularSlot(itemHandler, i));
                cap = 'i';
            } else if (recipeCap == FluidRecipeCapability.CAP) {
                NotifiableFluidTank fluidHandler = workableMachine.exportFluids;
                slotWidget = i -> new FluidSlot().syncHandler(fluidHandler.getStorages()[i]);
                cap = 'f';
            } else {
                continue;
            }

            int slotCapLimit = machine.getDefinition().getRecipeOutputLimits().getInt(recipeCap);
            if(slotCapLimit > 0) {
                max = Math.min(max, slotCapLimit);
            }
            int rows = calculateRowSize(max);
            int cols = max / rows;
            for (int i = 0; i < rows; i++) {
                StringBuilder s = new StringBuilder();
                for (int j = 0; j < cols; j++) {
                    if ((i * cols + j) >= max) {
                        s.append(" ");
                    } else {
                        s.append(cap);
                    }
                }
                outMatrix.add(s.toString());
            }

            outputSlotWidgets.put(cap, slotWidget);
        }

        if (!outputSlotWidgets.isEmpty()) {

            var slotBuilder = SlotGroupWidget.builder()
                    .matrix(outMatrix.toArray(new String[0]));

            for (var entry : outputSlotWidgets.entrySet()) {
                slotBuilder.key(entry.getKey(), entry.getValue());
            }
            var slotOutputWidget = slotBuilder.build();

            output.child(slotOutputWidget.align(Alignment.CenterLeft));
        }

        int maxRows = Math.max(inMatrix.size(), outMatrix.size());
        var mainUI = WidgetTree.findFirstWithNameNullable(panel, "MainUI", Row.class);
        if (mainUI != null) {
            //mainUI.getArea().setHeight(mainUI.getArea().getHeight() - ((2 - maxRows) * 9));
        }
    };

    private static int calculateRowSize(int max) {
        return switch (max) {
            case 1, 2, 3 -> 1;
            case 4, 5, 6 -> 2;
            case 7, 8, 9 -> 3;
            default -> (int)Math.ceil(Math.sqrt(max));
        };
    }



    public static PanelFactory RECIPE_TYPE = GTMuiPanels.BASE_PANEL.andThen(RECIPE_SLOTS, GTMuiEditors.TITLE, GTMuiEditors.CHARGE_SLOT,
            GTMuiEditors.PROGRESS_BAR(GTGuiTextures.PROGRESS_BAR_ARROW, 30, ProgressWidget.Direction.RIGHT), GTMuiEditors.POWER_BUTTON);
}
