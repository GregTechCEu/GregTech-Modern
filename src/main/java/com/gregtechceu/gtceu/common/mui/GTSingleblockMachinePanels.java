package com.gregtechceu.gtceu.common.mui;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleGeneratorMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.mui.factory.PanelFactory;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeMachineWidget;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;

public class GTSingleblockMachinePanels {

    public static PanelFactory GENERAL_MACHINE = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                                  MetaMachine machine) -> {

        GTRecipeType type;
        RecipeLogic recipeLogic;
        boolean isSteam = false;

        if (machine instanceof SimpleTieredMachine simpleTieredMachine) {
            type = simpleTieredMachine.getRecipeType();
            recipeLogic = simpleTieredMachine.getRecipeLogic();
        } else if (machine instanceof SimpleSteamMachine simpleSteamMachine) {
            type = simpleSteamMachine.getRecipeType();
            recipeLogic = simpleSteamMachine.recipeLogic;
            isSteam = true;
        } else if (machine instanceof SimpleGeneratorMachine simpleGeneratorMachine) {
            type = simpleGeneratorMachine.getRecipeType();
            recipeLogic = simpleGeneratorMachine.recipeLogic;
        } else {
            GTCEu.LOGGER.error(
                    "{} is not a SimpleTieredMachine/SimpleGeneratorMachine/SimpleSteamMachine, cannot add slots to its content",
                    machine.getDefinition().getName());
            return new ModularPanel<>(machine.getDefinition().getName());
        }

        if (type.getUiLayout() == null) {
            GTCEu.LOGGER.error(
                    "Tried to draw a singleblock recipe type UI for {}, but it does not have a recipe type UI",
                    machine.getDefinition().getName());
            return new ModularPanel<>(machine.getDefinition().getName());
        }

        var builder = !isSteam ? MachineUIPanelBuilder.panelBuilder(machine).drawGTLogo(true) :
                MachineUIPanelBuilder.defaultSteamMachinePanelBuilder(machine);
        return builder.mainContents(
                (parent) -> {
                    // todo find input column and output column width based on cap size, use to offset the widget in opp
                    // direction
                    // float offset = type.maxOutputs.values().intStream().max().getAsInt() -
                    // type.maxInputs.values().intStream().max().getAsInt();

                    parent.child(
                            new GTRecipeTypeMachineWidget(type, syncManager, machine, recipeLogic::getProgressPercent));
                    // .left((int)(offset * ((type.getUiLayout().getProgressSize() / 2.f) + 2)));
                })
                .build(syncManager, settings)
                .excludeAreaInRecipeViewer();
    };
}
