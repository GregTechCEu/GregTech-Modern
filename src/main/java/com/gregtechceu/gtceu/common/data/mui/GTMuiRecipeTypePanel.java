package com.gregtechceu.gtceu.common.data.mui;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.drawable.text.TextRenderer;
import com.gregtechceu.gtceu.api.mui.factory.PanelFactory;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.theme.Theme;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.SingleChildWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.slot.FluidSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTGuiTheme;

import java.util.function.IntFunction;

public class GTMuiRecipeTypePanel {

    public static PanelFactory BARE_RECIPE_TYPE = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                                   MetaMachine machine) -> {

        if (!(machine instanceof WorkableTieredMachine workableMachine)) {
            GTCEu.LOGGER.error("{} is not a WorkableTieredMachine, can not add slots to its content",
                    machine.getDefinition().getName());
            return null;
        }

        var recipeType = workableMachine.getRecipeType();

        ModularPanel panel = new ModularPanel(machine.getDefinition().getName());

        // Get the title string first
        String title = machine.getDefinition().getLangValue();

// This wrapper widget will hold the background
        panel.child(new SingleChildWidget<>()
                .widthRel(1.0f)
                .coverChildrenHeight()
                .widgetTheme(GTGuiTheme.TEXT_TITLE)
                .child(
                        IKey.str(title)
                                .asWidget()
                                .widgetTheme(GTGuiTheme.TEXT_TITLE)
                                .marginLeft(5)
                                .marginRight(5)
                                .marginTop(5)
                                .marginBottom(1)
                )
        );


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

        x = 20;
        y = 20;

        for (var entry : recipeType.maxOutputs.object2IntEntrySet()) {
            var recipeCap = entry.getKey();
            int max = entry.getIntValue();
            SlotGroup group = new SlotGroup(recipeCap.name + "_out", max);
            slotGroups.put(recipeCap, IO.OUT, group);
            syncManager.registerSlotGroup(group);

            int side = (int) Math.ceil(Math.sqrt(max));
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
                        .right(x).top(y));
            }

            y += 18 * side;
        }

        panel.bindPlayerInventory();
        return panel;
    };



    public static PanelFactory RECIPE_TYPE = BARE_RECIPE_TYPE.andThen(GTMuiEditors.CHARGE_SLOT, GTMuiEditors.PROGRESS_BAR);
}
