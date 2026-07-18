package com.gregtechceu.gtceu.api.recipe.gui;

import com.gregtechceu.gtceu.api.capability.recipe.*;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.utils.Alignment;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.layout.Flow;

/**
 * Builds and attaches the UI for a specific capability in a recipe viewer UI.
 */
@FunctionalInterface
public interface RecipeViewerCapabilityLayoutBuilder {

    /**
     * Builds and attaches the UI for a specific capability in a recipe viewer UI.
     *
     * @param layout The {@link GTRecipeTypeUILayout} which holds UI layout data.
     * @param widget The {@link GTRecipeViewerWidget} recipe widget.
     * @param io     The IO mode widgets are being created for.
     */
    void createCapabilityUILayout(GTRecipeTypeUILayout layout, GTRecipeViewerWidget widget, IO io);

    /**
     * The default recipe viewer UI layout for item slots.
     */
    RecipeViewerCapabilityLayoutBuilder ITEM = (layout, widget, io) -> {

        if (layout.getRecipeType().getMaxSlots(ItemRecipeCapability.CAP, io) == 0) return;

        if (layout.getRecipeType().getMaxSlots(ItemRecipeCapability.CAP, io) == 1) {
            var slot = RecipeViewerSlotWidget.create().value(ItemStackList.of(ItemStack.EMPTY))
                    .background(GuiTextures.SLOT_ITEM,
                            layout.capabilityInfo(ItemRecipeCapability.CAP).getOverlay(io, 0))
                    .name(GTRecipeViewerWidget.capabilityWidgetName(ItemRecipeCapability.CAP, io, 0));
            if (io == IO.IN) widget.inputColumn.child(slot);
            else widget.outputColumn.child(slot);
            return;
        }

        var slotGroupWidget = SlotGroupWidget
                .builder()
                .matrix(layout.capabilityInfo(ItemRecipeCapability.CAP).getRecipeViewerGrid(io))
                .key('s', i -> RecipeViewerSlotWidget.create().value(ItemStackList.of(ItemStack.EMPTY))
                        .background(GuiTextures.SLOT_ITEM,
                                layout.capabilityInfo(ItemRecipeCapability.CAP).getOverlay(io, i))
                        .name(GTRecipeViewerWidget.capabilityWidgetName(ItemRecipeCapability.CAP, io, i)))
                .build()
                .coverChildren(18, 18);

        if (io == IO.IN) widget.inputColumn.child(slotGroupWidget);
        else widget.outputColumn.child(slotGroupWidget);
    };

    /**
     * The default recipe viewer UI layout for fluid slots.
     */
    RecipeViewerCapabilityLayoutBuilder FLUID = (layout, widget, io) -> {

        if (layout.getRecipeType().getMaxSlots(FluidRecipeCapability.CAP, io) == 0) return;

        if (layout.getRecipeType().getMaxSlots(FluidRecipeCapability.CAP, io) == 1) {
            var slot = RecipeViewerSlotWidget.create().value(FluidStackList.of(FluidStack.EMPTY))
                    .background(GuiTextures.SLOT_FLUID,
                            layout.capabilityInfo(FluidRecipeCapability.CAP).getOverlay(io, 0))
                    .name(GTRecipeViewerWidget.capabilityWidgetName(FluidRecipeCapability.CAP, io, 0));
            if (io == IO.IN) widget.inputColumn.child(slot);
            else widget.outputColumn.child(slot);
            return;
        }

        var slotGroupWidget = SlotGroupWidget.builder()
                .matrix(layout.capabilityInfo(FluidRecipeCapability.CAP).getRecipeViewerGrid(io))
                .key('s', i -> RecipeViewerSlotWidget.create().value(FluidStackList.of(FluidStack.EMPTY))
                        .background(GuiTextures.SLOT_FLUID,
                                layout.capabilityInfo(FluidRecipeCapability.CAP).getOverlay(io, i))
                        .name(GTRecipeViewerWidget.capabilityWidgetName(FluidRecipeCapability.CAP, io, i)))
                .build()
                .coverChildren(18, 18);

        if (io == IO.IN) widget.inputColumn.child(slotGroupWidget);
        else widget.outputColumn.child(slotGroupWidget);
    };

    RecipeViewerCapabilityLayoutBuilder COMPUTATION = (layout, widget, io) -> {
        if (layout.getRecipeType().getMaxSlots(CWURecipeCapability.CAP, io) == 0) return;

        var computationInfo = Flow.col().childPadding(2)
                .coverChildrenHeight()
                .widthRel(1f)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .name(GTRecipeViewerWidget.capabilityWidgetName(CWURecipeCapability.CAP, io, 0));

        widget.textComponents.child(computationInfo);
    };

    RecipeViewerCapabilityLayoutBuilder EU = (layout, widget, io) -> {
        if (layout.getRecipeType().getMaxSlots(EURecipeCapability.CAP, io) == 0) return;

        widget.textComponents.child(Flow.col().childPadding(1)
                .coverChildrenHeight()
                .widthRel(1f)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .name(GTRecipeViewerWidget.capabilityWidgetName(EURecipeCapability.CAP, io, 0)));
    };
}
