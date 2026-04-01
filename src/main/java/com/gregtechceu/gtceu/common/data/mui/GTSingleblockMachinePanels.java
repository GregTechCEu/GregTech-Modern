package com.gregtechceu.gtceu.common.data.mui;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;
import com.gregtechceu.gtceu.api.mui.factory.PanelFactory;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.theme.ThemeAPI;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUIs;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

public class GTSingleblockMachinePanels {

    public static PanelFactory GENERAL_MACHINE = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                                  MetaMachine machine) -> {
        if (!(machine instanceof SimpleTieredMachine simpleTieredMachine)) {
            GTCEu.LOGGER.error("{} is not a WorkableTieredMachine, can not add slots to its content",
                    machine.getDefinition().getName());
            return new ModularPanel<>(machine.getDefinition().getName());
        }

        var inputItemGrid = GTMuiWidgets.createGrid(simpleTieredMachine.importItems.getSize(), 3, false, 'i');
        var inputFluidGrid = GTMuiWidgets.createGrid(simpleTieredMachine.importFluids.getSize(), 3, false, 'f');
        var outputItemGrid = GTMuiWidgets.createGrid(simpleTieredMachine.exportItems.getSize(), 3, true, 'i');
        var outputFluidGrid = GTMuiWidgets.createGrid(simpleTieredMachine.exportFluids.getSize(), 3, true, 'f');

        int slotHeight = Math.max(inputItemGrid.length + inputFluidGrid.length,
                outputItemGrid.length + outputFluidGrid.length);

        var panelBuilder = MachineUIPanelBuilder.defaultSimpleSingleblockPanelBuilder(machine);

        boolean hasXEI = GTRecipeTypeUIs.recipeTypeUIs.containsKey(simpleTieredMachine.getRecipeType());

        var theme = machine.getDefinition().getThemeId();

        panelBuilder.mainContents((parent) -> Flow.row()
                .size(MachineUIPanel.DEFAULT_CONTENT_WIDTH, 18 + 9 + 18 * Math.max(2, slotHeight))
                .childIf(hasXEI, () -> GTRecipeTypeUIs.recipeTypeUIs.get(simpleTieredMachine.getRecipeType())
                        .getBackedSlotsRow(syncManager, theme, simpleTieredMachine.importItems,
                                simpleTieredMachine.exportItems,
                                simpleTieredMachine.importFluids, simpleTieredMachine.exportFluids,
                                simpleTieredMachine.recipeLogic::getProgressPercent,
                                -1)));
        return panelBuilder.build(syncManager, settings).excludeAreaInRecipeViewer();
    };

    public static PanelFactory MACERATOR = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                            MetaMachine machine) -> {
        if (!(machine instanceof SimpleTieredMachine simpleTieredMachine)) {
            GTCEu.LOGGER.error("{} is not a SimpleTieredMachine, can not add slots to its content",
                    machine.getDefinition().getName());
            return new ModularPanel<>(machine.getDefinition().getName());
        }

        var inputItemGrid = GTMuiWidgets.createGrid(simpleTieredMachine.importItems.getSize(), 3, false, 'i');
        var inputFluidGrid = GTMuiWidgets.createGrid(simpleTieredMachine.importFluids.getSize(), 3, false, 'f');
        var outputItemGrid = GTMuiWidgets.createGrid(simpleTieredMachine.exportItems.getSize(), 3, true, 'i');
        var outputFluidGrid = GTMuiWidgets.createGrid(simpleTieredMachine.exportFluids.getSize(), 3, true, 'f');

        int slotHeight = Math.max(inputItemGrid.length + inputFluidGrid.length,
                outputItemGrid.length + outputFluidGrid.length);

        var panelBuilder = MachineUIPanelBuilder.defaultSimpleSingleblockPanelBuilder(machine);

        boolean hasXEI = GTRecipeTypeUIs.recipeTypeUIs.containsKey(simpleTieredMachine.getRecipeType());

        var theme = machine.getDefinition().getThemeId();

        panelBuilder.mainContents((parent) -> {
            parent.height(18 + 9 + 18 * Math.max(2, slotHeight));

            parent.child(Flow.row()
                    .size(MachineUIPanel.DEFAULT_CONTENT_WIDTH, 18 + 9 + 18 * Math.max(2, slotHeight))
                    .childIf(hasXEI, () -> GTRecipeTypeUIs.recipeTypeUIs.get(simpleTieredMachine.getRecipeType())
                            .getBackedSlotsRow(syncManager, theme, simpleTieredMachine.importItems,
                                    simpleTieredMachine.exportItems,
                                    simpleTieredMachine.importFluids, simpleTieredMachine.exportFluids,
                                    simpleTieredMachine.recipeLogic::getProgressPercent,
                                    simpleTieredMachine.getTier())
                            .posRel(Alignment.Center))
            // .left(7)
            );

            /*
             * parent.childIf(hasXEI, () ->
             * GTMuiWidgets.createXEIWidget(GTRecipeTypeUIs.recipeTypeUIs.get(workableMachine.getRecipeType()))
             * .left(190));
             */
        });
        return panelBuilder.build(syncManager, settings).excludeAreaInRecipeViewer();
    };

    public static PanelFactory ARC_FURNACE = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                              MetaMachine machine) -> {
        if (!(machine instanceof SimpleTieredMachine simpleTieredMachine)) {
            GTCEu.LOGGER.error("{} is not a WorkableTieredMachine, can not add slots to its content",
                    machine.getDefinition().getName());
            return new ModularPanel<>(machine.getDefinition().getName());
        }

        var inputItemGrid = GTMuiWidgets.createGrid(simpleTieredMachine.importItems.getSize(), 3, false, 'i');
        var inputFluidGrid = GTMuiWidgets.createGrid(simpleTieredMachine.importFluids.getSize(), 3, false, 'f');
        var outputItemGrid = GTMuiWidgets.createGrid(simpleTieredMachine.exportItems.getSize(), 3, true, 'i');
        var outputFluidGrid = GTMuiWidgets.createGrid(simpleTieredMachine.exportFluids.getSize(), 3, true, 'f');

        int slotHeight = Math.max(inputItemGrid.length + inputFluidGrid.length,
                outputItemGrid.length + outputFluidGrid.length);

        var panelBuilder = MachineUIPanelBuilder.defaultSimpleSingleblockPanelBuilder(machine);
        var theme = machine.getDefinition().getThemeId();

        boolean hasXEI = GTRecipeTypeUIs.recipeTypeUIs.containsKey(simpleTieredMachine.getRecipeType());

        panelBuilder.mainContents((parent) -> {
            parent.height(18 + 9 + 18 * Math.max(2, slotHeight));
            parent.child(Flow.row()
                    .size(MachineUIPanel.DEFAULT_CONTENT_WIDTH, 18 + 9 + 18 * Math.max(2, slotHeight))
                    .childIf(hasXEI, () -> GTRecipeTypeUIs.recipeTypeUIs.get(simpleTieredMachine.getRecipeType())
                            .getBackedSlotsRow(syncManager, theme, simpleTieredMachine.importItems,
                                    simpleTieredMachine.exportItems,
                                    simpleTieredMachine.importFluids, simpleTieredMachine.exportFluids,
                                    simpleTieredMachine.recipeLogic::getProgressPercent,
                                    0)
                            .posRel(Alignment.Center)));

            /*
             * parent.childIf(hasXEI, () ->
             * GTMuiWidgets.createXEIWidget(GTRecipeTypeUIs.recipeTypeUIs.get(workableMachine.getRecipeType()))
             * .left(190));
             */

        });
        return panelBuilder.build(syncManager, settings).excludeAreaInRecipeViewer();
    };

    public static PanelFactory STEAM_MACHINE = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                                MetaMachine machine) -> {
        if (!(machine instanceof SimpleSteamMachine steamMachine)) {
            GTCEu.LOGGER.error("{} is not a SimpleSteamMachine, can not add slots to its content",
                    machine.getDefinition().getName());
            return new ModularPanel<>(machine.getDefinition().getName());
        }

        var panelBuilder = MachineUIPanelBuilder.defaultSteamMachineBuilder(machine);

        var inputItemGrid = GTMuiWidgets.createGrid(steamMachine.importItems.getSize(), 3, false, 'i');
        var outputItemGrid = GTMuiWidgets.createGrid(steamMachine.exportItems.getSize(), 3, true, 'i');

        int slotHeight = Math.max(inputItemGrid.length,
                outputItemGrid.length);

        boolean hasXEI = GTRecipeTypeUIs.recipeTypeUIs.containsKey(steamMachine.getRecipeType());

        var theme = machine.getDefinition().getThemeId();
        var backgroundTexture = (UITexture) ThemeAPI.INSTANCE.getTheme(theme).getPanelTheme().getTheme()
                .getBackground();
        if (backgroundTexture == null) {
            backgroundTexture = GTGuiTextures.BACKGROUND;
        }

        panelBuilder.mainContents(parent -> {
            parent.height(18 + 9 + 18 * Math.max(2, slotHeight));
            parent.child(Flow.row()
                    .size(MachineUIPanel.DEFAULT_CONTENT_WIDTH, 18 + 9 + 18 * Math.max(2, slotHeight))
                    .childIf(hasXEI, () -> GTRecipeTypeUIs.recipeTypeUIs.get(steamMachine.getRecipeType())
                            .getBackedSlotsRow(syncManager, theme, steamMachine.importItems,
                                    steamMachine.exportItems,
                                    null, null,
                                    steamMachine.recipeLogic::getProgressPercent,
                                    steamMachine.getTier())
                            .posRel(Alignment.Center)));
        });

        return panelBuilder.build(syncManager, settings).excludeAreaInRecipeViewer();
    };
}
