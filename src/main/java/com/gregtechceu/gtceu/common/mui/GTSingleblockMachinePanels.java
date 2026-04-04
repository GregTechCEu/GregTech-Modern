package com.gregtechceu.gtceu.common.mui;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.api.mui.factory.PanelFactory;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUIs;

import brachy.modularui.drawable.UITexture;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.theme.ThemeAPI;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.layout.Flow;

public class GTSingleblockMachinePanels {

    public static PanelFactory GENERAL_MACHINE = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                                  MetaMachine machine) -> {
        ModularPanel<?> panel = new ModularPanel<>(machine.getDefinition().getName());
        if (!(machine instanceof WorkableTieredMachine workableMachine)) {
            GTCEu.LOGGER.error("{} is not a WorkableTieredMachine, can not add slots to its content",
                    machine.getDefinition().getName());
            return panel;
        }

        if (!(machine instanceof SimpleTieredMachine simpleTieredMachine)) {
            GTCEu.LOGGER.error("{} is not a WorkableTieredMachine, can not add slots to its content",
                    machine.getDefinition().getName());
            return panel;
        }

        var inputItemGrid = GTMuiWidgets.createGrid(workableMachine.importItems.getSize(), 3, false, 'i');
        var inputFluidGrid = GTMuiWidgets.createGrid(workableMachine.importFluids.getSize(), 3, false, 'f');
        var outputItemGrid = GTMuiWidgets.createGrid(workableMachine.exportItems.getSize(), 3, true, 'i');
        var outputFluidGrid = GTMuiWidgets.createGrid(workableMachine.exportFluids.getSize(), 3, true, 'f');

        int slotHeight = Math.max(inputItemGrid.length + inputFluidGrid.length,
                outputItemGrid.length + outputFluidGrid.length);

        boolean ghostCircuit = simpleTieredMachine.isCircuitSlotEnabled();

        panel.size(176, 76 + 21 + 18 + 9 + 18 * Math.max(2, slotHeight));

        boolean hasXEI = GTRecipeTypeUIs.recipeTypeUIs.containsKey(workableMachine.getRecipeType());

        var theme = machine.getDefinition().getThemeId();
        var backgroundTexture = (UITexture) ThemeAPI.INSTANCE.getTheme(theme).getPanelTheme().theme()
                .getBackground();
        if (backgroundTexture == null) {
            backgroundTexture = GTGuiTextures.BACKGROUND;
        }

        var autoOutput = machine.getTraitHolder().getTrait(AutoOutputTrait.TYPE);

        panel.child(GTMuiWidgets.createTitleBar(machine.getDefinition(), 176))
                .child(Flow.row()
                        .childIf(hasXEI, () -> GTRecipeTypeUIs.recipeTypeUIs.get(workableMachine.getRecipeType())
                                .getBackedSlotsRow(syncManager, theme, simpleTieredMachine.importItems,
                                        simpleTieredMachine.exportItems,
                                        simpleTieredMachine.importFluids, simpleTieredMachine.exportFluids,
                                        simpleTieredMachine.recipeLogic::getProgressPercent,
                                        -1)
                                .leftRel(0.5f))
                        .coverChildrenHeight()
                        // .left(7)
                        .bottom(76 + 7 + 18 + 9))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7))
                .child(Flow.col()
                        .coverChildren()
                        .leftRel(1.0f)
                        .reverseLayout(true)
                        .padding(2, 4, 4, 4)
                        .bottom(16)
                        .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                        .childPadding(2)
                        .background(backgroundTexture.getSubArea(0.25f, 0f, 1.0f, 1.0f))
                        .child(GTMuiWidgets.createPowerButton(workableMachine, syncManager))
                        .child(GTMuiWidgets.createBatterySlot(simpleTieredMachine, syncManager))
                        .childIf(autoOutput != null && autoOutput.supportsAutoOutputItems(),
                                () -> GTMuiWidgets.createAutoOutputItemButton(autoOutput, syncManager))
                        .childIf(autoOutput != null && autoOutput.supportsAutoOutputFluids(),
                                () -> GTMuiWidgets.createAutoOutputFluidButton(autoOutput, syncManager))
                        .childIf(autoOutput != null && autoOutput.supportsAutoOutputItems(),
                                () -> GTMuiWidgets.createInputFromOutputItem(autoOutput, syncManager))
                        .childIf(autoOutput != null && autoOutput.supportsAutoOutputFluids(),
                                () -> GTMuiWidgets.createInputFromOutputFluid(autoOutput, syncManager)))
                .child(Flow.col()
                        .coverChildren()
                        .rightRel(1.0f)
                        .reverseLayout(true)
                        .padding(4, 2, 4, 4)
                        .bottom(16)
                        .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                        .background(GTGuiTextures.BACKGROUND.getSubArea(0f, 0f, 0.75f, 1.0f))
                        .childIf(ghostCircuit,
                                () -> GTMuiWidgets.createCircuitSlotPanel(simpleTieredMachine, panel, syncManager)))
                .child(GTMuiWidgets.createGTLogo()
                        .right(7).bottom(7 + 78));
        if (hasXEI && false) {
            panel.child(GTMuiWidgets.createXEIWidget(GTRecipeTypeUIs.recipeTypeUIs.get(workableMachine.getRecipeType()))
                    .left(190));
        }
        panel.excludeAreaInRecipeViewer();
        return panel;
    };

    public static PanelFactory MACERATOR = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                            MetaMachine machine) -> {
        ModularPanel<?> panel = new ModularPanel<>(machine.getDefinition().getName());
        if (!(machine instanceof WorkableTieredMachine workableMachine)) {
            GTCEu.LOGGER.error("{} is not a WorkableTieredMachine, can not add slots to its content",
                    machine.getDefinition().getName());
            return panel;
        }

        if (!(machine instanceof SimpleTieredMachine simpleTieredMachine)) {
            GTCEu.LOGGER.error("{} is not a WorkableTieredMachine, can not add slots to its content",
                    machine.getDefinition().getName());
            return panel;
        }

        var inputItemGrid = GTMuiWidgets.createGrid(workableMachine.importItems.getSize(), 3, false, 'i');
        var inputFluidGrid = GTMuiWidgets.createGrid(workableMachine.importFluids.getSize(), 3, false, 'f');
        var outputItemGrid = GTMuiWidgets.createGrid(workableMachine.exportItems.getSize(), 3, true, 'i');
        var outputFluidGrid = GTMuiWidgets.createGrid(workableMachine.exportFluids.getSize(), 3, true, 'f');

        int slotHeight = Math.max(inputItemGrid.length + inputFluidGrid.length,
                outputItemGrid.length + outputFluidGrid.length);

        boolean ghostCircuit = simpleTieredMachine.isCircuitSlotEnabled();

        panel.size(176, 76 + 21 + 18 + 9 + 18 * slotHeight);

        boolean hasXEI = GTRecipeTypeUIs.recipeTypeUIs.containsKey(workableMachine.getRecipeType());

        var theme = machine.getDefinition().getThemeId();
        var backgroundTexture = (UITexture) ThemeAPI.INSTANCE.getTheme(theme).getPanelTheme().theme()
                .getBackground();
        if (backgroundTexture == null) {
            backgroundTexture = GTGuiTextures.BACKGROUND;
        }

        var autoOutput = machine.getTraitHolder().getTrait(AutoOutputTrait.TYPE);

        panel.child(GTMuiWidgets.createTitleBar(machine.getDefinition(), 176))
                .child(Flow.row()
                        .childIf(hasXEI, () -> GTRecipeTypeUIs.recipeTypeUIs.get(workableMachine.getRecipeType())
                                .getBackedSlotsRow(syncManager, theme, simpleTieredMachine.importItems,
                                        simpleTieredMachine.exportItems,
                                        simpleTieredMachine.importFluids, simpleTieredMachine.exportFluids,
                                        simpleTieredMachine.recipeLogic::getProgressPercent,
                                        simpleTieredMachine.getTier())
                                .leftRel(0.5f))
                        .coverChildrenHeight()
                        // .left(7)
                        .bottom(76 + 7 + 18 + 9))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7))
                .child(Flow.col()
                        .coverChildren()
                        .leftRel(1.0f)
                        .reverseLayout(true)
                        .bottom(16)
                        .padding(0, 8, 4, 4)
                        .childPadding(2)
                        .background(backgroundTexture.getSubArea(0.25f, 0f, 1.0f, 1.0f))
                        .child(GTMuiWidgets.createPowerButton(workableMachine, syncManager))
                        .child(GTMuiWidgets.createBatterySlot(simpleTieredMachine, syncManager))
                        .childIf(autoOutput != null && autoOutput.supportsAutoOutputItems(),
                                () -> GTMuiWidgets.createAutoOutputItemButton(autoOutput, syncManager))
                        .childIf(autoOutput != null && autoOutput.supportsAutoOutputFluids(),
                                () -> GTMuiWidgets.createAutoOutputFluidButton(autoOutput, syncManager))
                        .childIf(autoOutput != null && autoOutput.supportsAutoOutputItems(),
                                () -> GTMuiWidgets.createInputFromOutputItem(autoOutput, syncManager))
                        .childIf(autoOutput != null && autoOutput.supportsAutoOutputFluids(),
                                () -> GTMuiWidgets.createInputFromOutputFluid(autoOutput, syncManager)))
                .child(Flow.col()
                        .coverChildren()
                        .rightRel(1.0f)
                        .reverseLayout(true)
                        .padding(0, 8, 4, 4)
                        .bottom(16)
                        .background(GTGuiTextures.BACKGROUND.getSubArea(0f, 0f, 0.75f, 1.0f))
                        .childIf(ghostCircuit,
                                () -> GTMuiWidgets.createCircuitSlotPanel(simpleTieredMachine, panel, syncManager)))
                .child(GTMuiWidgets.createGTLogo()
                        .right(7).bottom(7 + 78));
        if (hasXEI && false) {
            panel.child(GTMuiWidgets.createXEIWidget(GTRecipeTypeUIs.recipeTypeUIs.get(workableMachine.getRecipeType()))
                    .left(190));
        }
        panel.excludeAreaInRecipeViewer();
        return panel;
    };

    public static PanelFactory ARC_FURNACE = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                              MetaMachine machine) -> {
        ModularPanel<?> panel = new ModularPanel<>(machine.getDefinition().getName());
        if (!(machine instanceof WorkableTieredMachine workableMachine)) {
            GTCEu.LOGGER.error("{} is not a WorkableTieredMachine, can not add slots to its content",
                    machine.getDefinition().getName());
            return panel;
        }

        if (!(machine instanceof SimpleTieredMachine simpleTieredMachine)) {
            GTCEu.LOGGER.error("{} is not a WorkableTieredMachine, can not add slots to its content",
                    machine.getDefinition().getName());
            return panel;
        }

        var inputItemGrid = GTMuiWidgets.createGrid(workableMachine.importItems.getSize(), 3, false, 'i');
        var inputFluidGrid = GTMuiWidgets.createGrid(workableMachine.importFluids.getSize(), 3, false, 'f');
        var outputItemGrid = GTMuiWidgets.createGrid(workableMachine.exportItems.getSize(), 3, true, 'i');
        var outputFluidGrid = GTMuiWidgets.createGrid(workableMachine.exportFluids.getSize(), 3, true, 'f');

        int slotHeight = Math.max(inputItemGrid.length + inputFluidGrid.length,
                outputItemGrid.length + outputFluidGrid.length);

        boolean ghostCircuit = simpleTieredMachine.isCircuitSlotEnabled();

        panel.size(176, 76 + 21 + 18 + 9 + 18 * slotHeight);

        var theme = machine.getDefinition().getThemeId();
        var backgroundTexture = (UITexture) ThemeAPI.INSTANCE.getTheme(theme).getPanelTheme().theme()
                .getBackground();
        if (backgroundTexture == null) {
            backgroundTexture = GTGuiTextures.BACKGROUND;
        }

        boolean hasXEI = GTRecipeTypeUIs.recipeTypeUIs.containsKey(workableMachine.getRecipeType());

        var autoOutput = machine.getTraitHolder().getTrait(AutoOutputTrait.TYPE);

        panel.child(GTMuiWidgets.createTitleBar(machine.getDefinition(), 176))
                .child(Flow.row()
                        .childIf(hasXEI, () -> GTRecipeTypeUIs.recipeTypeUIs.get(workableMachine.getRecipeType())
                                .getBackedSlotsRow(syncManager, theme, simpleTieredMachine.importItems,
                                        simpleTieredMachine.exportItems,
                                        simpleTieredMachine.importFluids, simpleTieredMachine.exportFluids,
                                        simpleTieredMachine.recipeLogic::getProgressPercent,
                                        0)
                                .leftRel(0.5f))
                        .coverChildrenHeight()
                        // .left(7)
                        .bottom(76 + 7 + 18 + 9))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7))
                .child(Flow.col()
                        .coverChildren()
                        .leftRel(1.0f)
                        .reverseLayout(true)
                        .bottom(16)
                        .padding(0, 8, 4, 4)
                        .childPadding(2)
                        .background(backgroundTexture.getSubArea(0.25f, 0f, 1.0f, 1.0f))
                        .child(GTMuiWidgets.createPowerButton(workableMachine, syncManager))
                        .child(GTMuiWidgets.createBatterySlot(simpleTieredMachine, syncManager))
                        .childIf(autoOutput != null && autoOutput.supportsAutoOutputItems(),
                                () -> GTMuiWidgets.createAutoOutputItemButton(autoOutput, syncManager))
                        .childIf(autoOutput != null && autoOutput.supportsAutoOutputFluids(),
                                () -> GTMuiWidgets.createAutoOutputFluidButton(autoOutput, syncManager))
                        .childIf(autoOutput != null && autoOutput.supportsAutoOutputItems(),
                                () -> GTMuiWidgets.createInputFromOutputItem(autoOutput, syncManager))
                        .childIf(autoOutput != null && autoOutput.supportsAutoOutputFluids(),
                                () -> GTMuiWidgets.createInputFromOutputFluid(autoOutput, syncManager)))
                .child(Flow.col()
                        .coverChildren()
                        .rightRel(1.0f)
                        .reverseLayout(true)
                        .padding(0, 8, 4, 4)
                        .bottom(16)
                        .background(GTGuiTextures.BACKGROUND.getSubArea(0f, 0f, 0.75f, 1.0f))
                        .childIf(ghostCircuit,
                                () -> GTMuiWidgets.createCircuitSlotPanel(simpleTieredMachine, panel, syncManager)))
                .child(GTMuiWidgets.createGTLogo()
                        .right(7).bottom(7 + 78));
        if (hasXEI && false) {
            panel.child(GTMuiWidgets.createXEIWidget(GTRecipeTypeUIs.recipeTypeUIs.get(workableMachine.getRecipeType()))
                    .left(190));
        }
        panel.excludeAreaInRecipeViewer();
        return panel;
    };

    public static PanelFactory STEAM_MACHINE = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                                MetaMachine machine) -> {
        ModularPanel<?> panel = new ModularPanel<>(machine.getDefinition().getName());
        if (!(machine instanceof SimpleSteamMachine steamMachine)) {
            GTCEu.LOGGER.error("{} is not a SimpleSteamMachine, can not add slots to its content",
                    machine.getDefinition().getName());
            return panel;
        }
        // panel.widgetTheme(GTGuiTheme.BRONZE.getId());

        var inputItemGrid = GTMuiWidgets.createGrid(steamMachine.importItems.getSize(), 3, false, 'i');
        var outputItemGrid = GTMuiWidgets.createGrid(steamMachine.exportItems.getSize(), 3, true, 'i');

        int slotHeight = Math.max(inputItemGrid.length, outputItemGrid.length);

        panel.size(176, 76 + 21 + 18 + 9 + 18 * Math.max(2, slotHeight));

        boolean hasXEI = GTRecipeTypeUIs.recipeTypeUIs.containsKey(steamMachine.getRecipeType());

        var theme = machine.getDefinition().getThemeId();
        var backgroundTexture = (UITexture) ThemeAPI.INSTANCE.getTheme(theme).getPanelTheme().theme()
                .getBackground();
        if (backgroundTexture == null) {
            backgroundTexture = GTGuiTextures.BACKGROUND;
        }

        panel.child(GTMuiWidgets.createTitleBar(machine.getDefinition(), 176))
                .child(Flow.row()
                        .childIf(hasXEI, () -> GTRecipeTypeUIs.recipeTypeUIs.get(steamMachine.getRecipeType())
                                .getBackedSlotsRow(syncManager, theme, steamMachine.importItems,
                                        steamMachine.exportItems,
                                        null, null,
                                        steamMachine.recipeLogic::getProgressPercent,
                                        steamMachine.getTier())
                                .leftRel(0.5f))
                        .coverChildrenHeight()
                        // .left(7)
                        .bottom(76 + 7 + 18 + 9))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7))
                .child(Flow.col()
                        .coverChildren()
                        .leftRel(1.0f)
                        .reverseLayout(true)
                        .bottom(16)
                        .padding(0, 8, 4, 4)
                        .childPadding(2)
                        .background(backgroundTexture.getSubArea(0.25f, 0f, 1.0f, 1.0f))
                        .child(GTMuiWidgets.createPowerButton(steamMachine, syncManager)))
                .child(GTMuiWidgets.createGTLogo()
                        .right(7).bottom(7 + 78));

        return panel;
    };
}
