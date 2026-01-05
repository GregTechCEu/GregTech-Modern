package com.gregtechceu.gtceu.common.data.mui;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.widget.EmptyWidget;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.api.mui.widgets.DynamicSyncedWidget;

public class GTMultiblockPanelUtil {

    private MultiblockControllerMachine controller;

    public GTMultiblockPanelUtil(MultiblockControllerMachine controller) {
        this.controller = controller;
    }

    public Widget<?> getMainTextPanel() {
        return null;
    }

    public IWidget getRecipeActionWidget() {
        if (controller instanceof IRecipeLogicMachine recipeLogicMachine) {
            RecipeLogic rl = recipeLogicMachine.getRecipeLogic();
            DynamicSyncedWidget<?> recipeActionResultWidget = new DynamicSyncedWidget<>();

            if (rl.isWaiting()) {
                var reason = rl.getWaitingReason();
                //recipeActionResultWidget.
                //recipeActionResultWidget = rl
            }
        }
        return new EmptyWidget();
    }
}
