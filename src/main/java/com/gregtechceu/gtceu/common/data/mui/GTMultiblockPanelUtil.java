package com.gregtechceu.gtceu.common.data.mui;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.widget.EmptyWidget;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widget.ScrollWidget;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.api.mui.widget.scroll.ScrollArea;
import com.gregtechceu.gtceu.api.mui.widgets.DynamicSyncedWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ListWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ScrollingTextWidget;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

public class GTMultiblockPanelUtil {

    private MultiblockControllerMachine controller;

    public GTMultiblockPanelUtil(MultiblockControllerMachine controller) {
        this.controller = controller;
    }

    public Widget<?> getMainTextPanel() {

        boolean isFormed = controller.isFormed();

        var parentWidget = new ListWidget<>();
        parentWidget.size(187, 90)
                .child(new IDrawable.DrawableWidget(GTGuiTextures.MUI_DISPLAY).widthRel(1.0f).heightRel(1.0f));

        if (controller instanceof IRecipeLogicMachine rlMachine) {
            var recipeLogic = rlMachine.getRecipeLogic();
            boolean isActive = recipeLogic.isActive();
            boolean isWorking = recipeLogic.isWorking();

            parentWidget.child(IKey.dynamic(() ->
                            GTMultiblockTextUtil.addProgressLine(isFormed, isActive, recipeLogic.getProgress(),
                                    recipeLogic.getMaxProgress(), recipeLogic.getProgressPercent())).asWidget());
        }
        return parentWidget;
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
