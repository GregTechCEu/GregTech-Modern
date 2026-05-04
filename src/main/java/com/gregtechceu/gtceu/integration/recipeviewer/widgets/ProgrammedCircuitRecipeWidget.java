package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;

import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;

public class ProgrammedCircuitRecipeWidget extends ParentWidget<ProgrammedCircuitRecipeWidget> {

    public ProgrammedCircuitRecipeWidget() {
        super();
        size(150, 80);

        Grid circuits = new Grid()
                .coverChildren()
                .gridOfSizeWidth(32, 8, (x, y, i) -> RecipeViewerSlotWidget.create()
                        .recipeSlotRole(RecipeSlotRole.RENDER_ONLY)
                        .value(IntCircuitBehaviour.stack(i + 1)));

        child(circuits.center());
    }
}
