package com.gregtechceu.gtceu.api.recipe.gui;

import brachy.modularui.api.GuiAxis;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import java.util.function.DoubleSupplier;

/**
 * The UI for singleblock recipe machines.
 */
public class GTRecipeTypeMachineWidget extends Flow {

    public final Flow inputColumn = Flow.col().coverChildren();
    public final Flow outputColumn = Flow.col().coverChildren();

    public GTRecipeTypeMachineWidget(GTRecipeType recipeType, PanelSyncManager syncManager,
                                     MetaMachine machine,
                                     DoubleSupplier progressSupplier) {
        super(GuiAxis.X);

        if (recipeType.getUiLayout() == null) {
            GTCEu.LOGGER.error(
                    "Tried to draw a singleblock recipe type UI for {}, but it does not have a recipe type UI",
                    machine.getDefinition().getName());
            return;
        }

        var layout = recipeType.getUiLayout();

        DoubleSyncValue progressPercent = syncManager.getOrCreateSyncHandler("progressPercent",
                DoubleSyncValue.class, () -> new DoubleSyncValue(progressSupplier));

        coverChildren();
        center();
        childPadding((layout.getProgressSize() / 2) + 2);
        child(inputColumn);
        child(layout.getProgressWidgetSupplier().get(layout, progressPercent));
        child(outputColumn);

        for (var entry: recipeType.maxInputs.object2IntEntrySet()) {
            var layoutFunc = layout.capabilityInfo(entry.getKey()).machineLayoutBuilder;
            if (layoutFunc == null || entry.getIntValue() == 0) continue;
            layoutFunc.createCapabilityUILayout(machine, layout, this, IO.IN);

        }

        for (var entry: recipeType.maxOutputs.object2IntEntrySet()) {
            var layoutFunc = layout.capabilityInfo(entry.getKey()).machineLayoutBuilder;
            if (layoutFunc == null || entry.getIntValue() == 0) continue;
            layoutFunc.createCapabilityUILayout(machine, layout, this, IO.OUT);
        }
    }
}
