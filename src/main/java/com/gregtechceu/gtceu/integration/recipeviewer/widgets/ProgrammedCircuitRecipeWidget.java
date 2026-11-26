package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;

import net.minecraft.world.item.ItemStack;

import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Grid;

public class ProgrammedCircuitRecipeWidget extends ParentWidget<ProgrammedCircuitRecipeWidget> {

    public ProgrammedCircuitRecipeWidget() {
        super();
        size(150, 80);

        Grid circuits = new Grid()
                .coverChildren()
                .gridOfSizeWidth(32, 8, (x, y, i) -> RecipeViewerSlotWidget.create(ItemStack.class)
                        .recipeSlotRole(RecipeSlotRole.RENDER_ONLY)
                        .value(IntCircuitBehaviour.stack(i + 1)));

        child(circuits.center());
    }
}
