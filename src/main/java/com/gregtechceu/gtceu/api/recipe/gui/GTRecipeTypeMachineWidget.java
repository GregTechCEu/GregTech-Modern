package com.gregtechceu.gtceu.api.recipe.gui;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IGuiAction;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import brachy.modularui.api.GuiAxis;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.integration.recipeviewer.emi.recipe.GTRecipeEMICategory;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.GTJEIPlugin;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.recipe.GTRecipeJEICategory;
import com.gregtechceu.gtceu.integration.recipeviewer.rei.recipe.GTRecipeREICategory;
import dev.emi.emi.api.EmiApi;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;

import java.util.List;
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
        childPadding((layout.getProgressSize() / 2) + 2);
        child(inputColumn);

        var progressWidget = layout.getProgressWidgetSupplier().get(layout, progressPercent);

        progressWidget.listenGuiAction((IGuiAction.MousePressed) (guiContext, i) -> {
            if (!guiContext.isMouseAbove(progressWidget)) return false;
            if (!recipeType.getCategory().isXEIVisible()) return false;
            if (GTCEu.Mods.isEMILoaded()) {
                EmiCallWrapper.openRecipeCategory(recipeType.getCategory());
            } else if (GTCEu.Mods.isJEILoaded()) {
                JeiCallWrapper.openRecipeCategory(recipeType.getCategory());
            } else if (GTCEu.Mods.isREILoaded()) {
                ReiCallWrapper.openRecipeCategory(recipeType.getCategory());
            }
            return true;
        });

        child(progressWidget.tooltip(r -> r.addLine(Text.lang("gtceu.recipe_type.show_recipes"))));
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


    private static class EmiCallWrapper {
        public static void openRecipeCategory(GTRecipeCategory category) {
            EmiApi.displayRecipeCategory(GTRecipeEMICategory.machineCategory(category));
        }
    }

    private static class JeiCallWrapper {
        public static void openRecipeCategory(GTRecipeCategory category) {
            GTJEIPlugin.getRuntime().getRecipesGui().showTypes(List.of(GTRecipeJEICategory.machineType(category)));
        }

    }

    private static class ReiCallWrapper {
        public static void openRecipeCategory(GTRecipeCategory category) {
            ViewSearchBuilder.builder().addCategories(List.of(GTRecipeREICategory.machineCategory(category))).open();
        }
    }
}
