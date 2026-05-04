package com.gregtechceu.gtceu.api.recipe.gui;

import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import java.util.Objects;

/**
 * Builds and attaches the UI for a specific capability in a recipe viewer UI.
 */
@FunctionalInterface
public interface RecipeViewerCapabilityLayoutBuilder {

    /**
     * Builds and attaches the UI for a specific capability in a recipe viewer UI.
     *
     * @param recipe The recipe this UI is for.
     * @param layout The {@link GTRecipeTypeUILayout} which holds UI layout data.
     * @param widget The {@link GTRecipeViewerWidget} recipe widget.
     * @param io     The IO mode widgets are being created for.
     */
    void createCapabilityUILayout(GTRecipe recipe, GTRecipeTypeUILayout layout, GTRecipeViewerWidget widget, IO io);

    /**
     * The default recipe viewer UI layout for item slots.
     */
    RecipeViewerCapabilityLayoutBuilder ITEM = (recipe, layout, widget, io) -> {

        if (layout.getRecipeType().getMaxSlots(ItemRecipeCapability.CAP, io) == 0) return;

        var slotGroupWidget = SlotGroupWidget
                .builder()
                .matrix(layout.capabilityInfo(ItemRecipeCapability.CAP).getRecipeViewerGrid(io, recipe))
                .key('s', i ->
                        Objects.requireNonNull(layout.capabilityInfo(ItemRecipeCapability.CAP).capabilityWidgetBuilder).buildDefaultWidget()
                        .background(GuiTextures.SLOT_ITEM, layout.capabilityInfo(ItemRecipeCapability.CAP).getOverlay(io, i))
                                .name(GTRecipeViewerWidget.capabilityWidgetName(ItemRecipeCapability.CAP, io, i))
                )
                .build()
                .coverChildren();

        if (io == IO.IN) widget.inputColumn.child(slotGroupWidget);
        else widget.outputColumn.child(slotGroupWidget);
    };

    /**
     * The default recipe viewer UI layout for fluid slots.
     */
    RecipeViewerCapabilityLayoutBuilder FLUID = (recipe, layout, widget, io) -> {

        if (layout.getRecipeType().getMaxSlots(FluidRecipeCapability.CAP, io) == 0) return;

        var slotGroupWidget = SlotGroupWidget.builder()
                .matrix(layout.capabilityInfo(FluidRecipeCapability.CAP).getRecipeViewerGrid(io, recipe))
                .key('s', i ->
                        Objects.requireNonNull(layout.capabilityInfo(FluidRecipeCapability.CAP).capabilityWidgetBuilder).buildDefaultWidget()
                                .background(GuiTextures.SLOT_FLUID, layout.capabilityInfo(FluidRecipeCapability.CAP).getOverlay(io, i))
                                .name(GTRecipeViewerWidget.capabilityWidgetName(FluidRecipeCapability.CAP, io, i))
                )
                .build()
                .coverChildren();

        if (io == IO.IN) widget.inputColumn.child(slotGroupWidget);
        else widget.outputColumn.child(slotGroupWidget);
    };
}
