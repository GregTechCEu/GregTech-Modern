package com.gregtechceu.gtceu.api.recipe.gui;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;

import brachy.modularui.api.GuiAxis;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IGuiAction;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;

import java.util.function.DoubleSupplier;

/**
 * The UI for singleblock recipe machines.
 */
public class GTRecipeTypeMachineWidget extends Flow {

    public final Flow inputColumn = Flow.col().coverChildren().crossAxisAlignment(Alignment.CrossAxis.START);
    public final Flow outputColumn = Flow.col().coverChildren().crossAxisAlignment(Alignment.CrossAxis.START);

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
        childPadding((layout.getProgressBar().progressSize() / 2) + 2);
        child(inputColumn);

        var progressWidget = layout.getProgressWidgetSupplier().get(layout, progressPercent, machine);

        progressWidget.listenGuiAction((IGuiAction.MousePressed) (guiContext, i) -> {
            if (!guiContext.isMouseAbove(progressWidget)) return false;
            if (!recipeType.getCategory().isXEIVisible()) return false;
            GTUtil.openRecipeViewerCategory(recipeType.getCategory());
            return true;
        });

        child(progressWidget.tooltip(r -> r.add(Text.comp(Component.translatable("gtceu.recipe_type.show_recipes")))));
        child(outputColumn);

        for (var entry : recipeType.maxInputs.object2IntEntrySet()) {
            var layoutFunc = layout.capabilityInfo(entry.getKey()).machineLayoutBuilder;
            if (layoutFunc == null || entry.getIntValue() == 0) continue;
            layoutFunc.createCapabilityUILayout(machine, layout, this, IO.IN);

        }

        for (var entry : recipeType.maxOutputs.object2IntEntrySet()) {
            var layoutFunc = layout.capabilityInfo(entry.getKey()).machineLayoutBuilder;
            if (layoutFunc == null || entry.getIntValue() == 0) continue;
            layoutFunc.createCapabilityUILayout(machine, layout, this, IO.OUT);
        }
    }
}
