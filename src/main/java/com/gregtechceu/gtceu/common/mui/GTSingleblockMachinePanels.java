package com.gregtechceu.gtceu.common.mui;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.mui.factory.PanelFactory;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUIs;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;

public class GTSingleblockMachinePanels {

    public static PanelFactory GENERAL_MACHINE = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                                  MetaMachine machine) -> {
        if (!(machine instanceof SimpleTieredMachine simpleTieredMachine)) {
            GTCEu.LOGGER.error("{} is not a WorkableTieredMachine, can not add slots to its content",
                    machine.getDefinition().getName());
            return new ModularPanel<>(machine.getDefinition().getName());
        }

        return MachineUIPanelBuilder.defaultSimpleSingleblockPanelBuilder(machine).mainContents((parent) -> {
            var inputItemGrid = GTMuiWidgets.createGrid(simpleTieredMachine.importItems.getSize(), 3, false, 'i');
            var inputFluidGrid = GTMuiWidgets.createGrid(simpleTieredMachine.importFluids.getSize(), 3, false, 'f');
            var outputItemGrid = GTMuiWidgets.createGrid(simpleTieredMachine.exportItems.getSize(), 3, true, 'i');
            var outputFluidGrid = GTMuiWidgets.createGrid(simpleTieredMachine.exportFluids.getSize(), 3, true, 'f');

            int slotHeight = Math.max(inputItemGrid.length + inputFluidGrid.length,
                    outputItemGrid.length + outputFluidGrid.length);
            boolean hasXEI = GTRecipeTypeUIs.recipeTypeUIs.containsKey(simpleTieredMachine.getRecipeType());

            var theme = machine.getDefinition().getThemeId();

            parent.child(Flow.row()
                    .size(MachineUIPanel.DEFAULT_CONTENT_WIDTH, 18 + 9 + 18 * Math.max(2, slotHeight))
                    .childIf(hasXEI, () -> GTRecipeTypeUIs.recipeTypeUIs.get(simpleTieredMachine.getRecipeType())
                            .getBackedSlotsRow(syncManager, theme, simpleTieredMachine.importItems,
                                    simpleTieredMachine.exportItems,
                                    simpleTieredMachine.importFluids, simpleTieredMachine.exportFluids,
                                    simpleTieredMachine.recipeLogic::getProgressPercent,
                                    -1)));
        }).build(syncManager, settings).excludeAreaInRecipeViewer();
    };

    public static PanelFactory MACERATOR = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                            MetaMachine machine) -> {
        if (!(machine instanceof SimpleTieredMachine simpleTieredMachine)) {
            GTCEu.LOGGER.error("{} is not a SimpleTieredMachine, can not add slots to its content",
                    machine.getDefinition().getName());
            return new ModularPanel<>(machine.getDefinition().getName());
        }

        return MachineUIPanelBuilder.defaultSimpleSingleblockPanelBuilder(machine).mainContents((parent) -> {

            var inputItemGrid = GTMuiWidgets.createGrid(simpleTieredMachine.importItems.getSize(), 3, false, 'i');
            var inputFluidGrid = GTMuiWidgets.createGrid(simpleTieredMachine.importFluids.getSize(), 3, false, 'f');
            var outputItemGrid = GTMuiWidgets.createGrid(simpleTieredMachine.exportItems.getSize(), 3, true, 'i');
            var outputFluidGrid = GTMuiWidgets.createGrid(simpleTieredMachine.exportFluids.getSize(), 3, true, 'f');

            int slotHeight = Math.max(inputItemGrid.length + inputFluidGrid.length,
                    outputItemGrid.length + outputFluidGrid.length);
            boolean hasXEI = GTRecipeTypeUIs.recipeTypeUIs.containsKey(simpleTieredMachine.getRecipeType());

            var theme = machine.getDefinition().getThemeId();

            parent.child(Flow.row()
                    .size(MachineUIPanel.DEFAULT_CONTENT_WIDTH, 18 + 9 + 18 * Math.max(2, slotHeight))
                    .childIf(hasXEI, () -> GTRecipeTypeUIs.recipeTypeUIs.get(simpleTieredMachine.getRecipeType())
                            .getBackedSlotsRow(syncManager, theme, simpleTieredMachine.importItems,
                                    simpleTieredMachine.exportItems,
                                    simpleTieredMachine.importFluids, simpleTieredMachine.exportFluids,
                                    simpleTieredMachine.recipeLogic::getProgressPercent,
                                    simpleTieredMachine.getTier())
                            .posRel(Alignment.Center)));
        }).build(syncManager, settings).excludeAreaInRecipeViewer();
    };

    public static PanelFactory ARC_FURNACE = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                              MetaMachine machine) -> {
        if (!(machine instanceof SimpleTieredMachine simpleTieredMachine)) {
            GTCEu.LOGGER.error("{} is not a WorkableTieredMachine, can not add slots to its content",
                    machine.getDefinition().getName());
            return new ModularPanel<>(machine.getDefinition().getName());
        }

        return MachineUIPanelBuilder.defaultSimpleSingleblockPanelBuilder(machine).mainContents((parent) -> {
            var inputItemGrid = GTMuiWidgets.createGrid(simpleTieredMachine.importItems.getSize(), 3, false, 'i');
            var inputFluidGrid = GTMuiWidgets.createGrid(simpleTieredMachine.importFluids.getSize(), 3, false, 'f');
            var outputItemGrid = GTMuiWidgets.createGrid(simpleTieredMachine.exportItems.getSize(), 3, true, 'i');
            var outputFluidGrid = GTMuiWidgets.createGrid(simpleTieredMachine.exportFluids.getSize(), 3, true, 'f');

            int slotHeight = Math.max(inputItemGrid.length + inputFluidGrid.length,
                    outputItemGrid.length + outputFluidGrid.length);

            var theme = machine.getDefinition().getThemeId();

            boolean hasXEI = GTRecipeTypeUIs.recipeTypeUIs.containsKey(simpleTieredMachine.getRecipeType());

            parent.child(Flow.row()
                    .size(MachineUIPanel.DEFAULT_CONTENT_WIDTH, 18 + 9 + 18 * Math.max(2, slotHeight))
                    .childIf(hasXEI, () -> GTRecipeTypeUIs.recipeTypeUIs.get(simpleTieredMachine.getRecipeType())
                            .getBackedSlotsRow(syncManager, theme, simpleTieredMachine.importItems,
                                    simpleTieredMachine.exportItems,
                                    simpleTieredMachine.importFluids, simpleTieredMachine.exportFluids,
                                    simpleTieredMachine.recipeLogic::getProgressPercent,
                                    0)
                            .posRel(Alignment.Center)));
        }).build(syncManager, settings).excludeAreaInRecipeViewer();
    };

    public static PanelFactory STEAM_MACHINE = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                                MetaMachine machine) -> {
        if (!(machine instanceof SimpleSteamMachine steamMachine)) {
            GTCEu.LOGGER.error("{} is not a SimpleSteamMachine, can not add slots to its content",
                    machine.getDefinition().getName());
            return new ModularPanel<>(machine.getDefinition().getName());
        }

        return MachineUIPanelBuilder.defaultSteamMachineBuilder(machine).mainContents(parent -> {
            var inputItemGrid = GTMuiWidgets.createGrid(steamMachine.importItems.getSize(), 3, false, 'i');
            var outputItemGrid = GTMuiWidgets.createGrid(steamMachine.exportItems.getSize(), 3, true, 'i');

            int slotHeight = Math.max(inputItemGrid.length,
                    outputItemGrid.length);

            boolean hasXEI = GTRecipeTypeUIs.recipeTypeUIs.containsKey(steamMachine.getRecipeType());

            var theme = machine.getDefinition().getThemeId();

            parent.child(Flow.row()
                    .size(MachineUIPanel.DEFAULT_CONTENT_WIDTH, 18 + 9 + 18 * Math.max(2, slotHeight))
                    .childIf(hasXEI, () -> GTRecipeTypeUIs.recipeTypeUIs.get(steamMachine.getRecipeType())
                            .getBackedSlotsRow(syncManager, theme, steamMachine.importItems,
                                    steamMachine.exportItems,
                                    null, null,
                                    steamMachine.recipeLogic::getProgressPercent,
                                    steamMachine.getTier())
                            .posRel(Alignment.Center)));
        }).build(syncManager, settings).excludeAreaInRecipeViewer();
    };
}
