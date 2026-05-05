package com.gregtechceu.gtceu.api.recipe.gui;

import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

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

        if (layout.getRecipeType().getMaxSlots(ItemRecipeCapability.CAP, io) == 1) {
            var slot = RecipeViewerSlotWidget.create().value(ItemStackList.of(ItemStack.EMPTY))
                    .background(GuiTextures.SLOT_ITEM, layout.capabilityInfo(ItemRecipeCapability.CAP).getOverlay(io, 0))
                    .name(GTRecipeViewerWidget.capabilityWidgetName(ItemRecipeCapability.CAP, io, 0));
            if (io == IO.IN) widget.inputColumn.child(slot);
            else widget.outputColumn.child(slot);
            return;
        }

        var slotGroupWidget = SlotGroupWidget
                .builder()
                .matrix(layout.capabilityInfo(ItemRecipeCapability.CAP).getRecipeViewerGrid(io, recipe))
                .key('s', i ->
                        RecipeViewerSlotWidget.create().value(ItemStackList.of(ItemStack.EMPTY))
                        .background(GuiTextures.SLOT_ITEM, layout.capabilityInfo(ItemRecipeCapability.CAP).getOverlay(io, i))
                                .name(GTRecipeViewerWidget.capabilityWidgetName(ItemRecipeCapability.CAP, io, i))
                )
                .build()
                .coverChildren(18, 18);

        if (io == IO.IN) widget.inputColumn.child(slotGroupWidget);
        else widget.outputColumn.child(slotGroupWidget);
    };

    /**
     * The default recipe viewer UI layout for fluid slots.
     */
    RecipeViewerCapabilityLayoutBuilder FLUID = (recipe, layout, widget, io) -> {

        if (layout.getRecipeType().getMaxSlots(FluidRecipeCapability.CAP, io) == 0) return;

        if (layout.getRecipeType().getMaxSlots(FluidRecipeCapability.CAP, io) == 1) {
            var slot = RecipeViewerSlotWidget.create().value(FluidStackList.of(FluidStack.EMPTY))
                    .background(GuiTextures.SLOT_FLUID, layout.capabilityInfo(FluidRecipeCapability.CAP).getOverlay(io, 0))
                    .name(GTRecipeViewerWidget.capabilityWidgetName(FluidRecipeCapability.CAP, io, 0));
            if (io == IO.IN) widget.inputColumn.child(slot);
            else widget.outputColumn.child(slot);
            return;
        }


        var slotGroupWidget = SlotGroupWidget.builder()
                .matrix(layout.capabilityInfo(FluidRecipeCapability.CAP).getRecipeViewerGrid(io, recipe))
                .key('s', i ->
                        RecipeViewerSlotWidget.create().value(FluidStackList.of(FluidStack.EMPTY))
                                .background(GuiTextures.SLOT_FLUID, layout.capabilityInfo(FluidRecipeCapability.CAP).getOverlay(io, i))
                                .name(GTRecipeViewerWidget.capabilityWidgetName(FluidRecipeCapability.CAP, io, i))
                )
                .build()
                .coverChildren(18, 18);

        if (io == IO.IN) widget.inputColumn.child(slotGroupWidget);
        else widget.outputColumn.child(slotGroupWidget);
    };
}
