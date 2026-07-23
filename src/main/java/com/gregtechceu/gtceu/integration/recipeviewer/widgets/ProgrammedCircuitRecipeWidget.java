package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.layout.Grid;

import net.minecraft.world.item.ItemStack;

public class ProgrammedCircuitRecipeWidget extends ParentWidget<ProgrammedCircuitRecipeWidget> {

    public ProgrammedCircuitRecipeWidget() {
        super();
        size(150, 80);

        Grid circuits = new Grid()
                .coverChildren()
                .gridOfSizeWidth(32, 8, (x, y, i) -> createSlot(IntCircuitBehaviour.stack(i + 1)));

        child(circuits.center());
    }

    /** Try RecipeViewerSlotWidget, fall back to ButtonWidget if JeiRecipeViewerSlot is broken */
    private static IWidget createSlot(ItemStack stack) {
        try {
            return RecipeViewerSlotWidget.create()
                    .recipeSlotRole(RecipeSlotRole.RENDER_ONLY)
                    .value(stack);
        } catch (Exception e) {
            return new ButtonWidget<>()
                    .overlay(new ItemDrawable(stack).asIcon().center())
                    .size(16);
        }
    }
}
