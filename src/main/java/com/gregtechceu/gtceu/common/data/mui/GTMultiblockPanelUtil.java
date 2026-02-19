package com.gregtechceu.gtceu.common.data.mui;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.drawable.Icon;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.sync.*;
import com.gregtechceu.gtceu.api.mui.widget.EmptyWidget;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.api.mui.widgets.DynamicSyncedWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ListWidget;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

public class GTMultiblockPanelUtil {

    private MultiblockControllerMachine controller;

    public GTMultiblockPanelUtil(MultiblockControllerMachine controller) {
        this.controller = controller;
    }

    public Widget<?> getMainTextPanel(PanelSyncManager syncManager, int width, int height) {
        boolean isFormed = controller.isFormed();

        var parentWidget = new ParentWidget<>();
        var listWidget = new ListWidget<>()
                .width(width - 6)
                .height(height - 6)
                .childSeparator(Icon.EMPTY_2PX)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .alignX(Alignment.CenterLeft);
        parentWidget.size(width, height)
                .background(GTGuiTextures.MUI_DISPLAY);

        if (controller instanceof WorkableMultiblockMachine rlMachine) {
            listWidget.child(GTMultiblockTextUtil.addProgressLine(rlMachine, syncManager));

            if (rlMachine instanceof WorkableElectricMultiblockMachine workableElectricMachine) {
                listWidget.child(GTMultiblockTextUtil.addEnergyTierLine(workableElectricMachine, syncManager));
                listWidget.child(GTMultiblockTextUtil.addEnergyUsageLine(workableElectricMachine, syncManager));
            }
            listWidget.child(GTMultiblockTextUtil.addWorkingStatusLine(rlMachine, syncManager));
            listWidget.child(GTMultiblockTextUtil.addParallelLine(rlMachine, syncManager));
            listWidget.child(GTMultiblockTextUtil.addBatchModeLine(rlMachine, syncManager));
            listWidget.child(GTMultiblockTextUtil.addSubtickParallelsLine(rlMachine, syncManager));
            listWidget.child(GTMultiblockTextUtil.addTotalRunsLine(rlMachine, syncManager));
            listWidget.child(GTMultiblockTextUtil.addOutputLines(rlMachine, syncManager));
        }
        parentWidget.child(listWidget.left(3).top(3));
        return parentWidget;
    }

    public IWidget getRecipeActionWidget() {
        if (controller instanceof IRecipeLogicMachine recipeLogicMachine) {
            RecipeLogic rl = recipeLogicMachine.getRecipeLogic();
            DynamicSyncedWidget<?> recipeActionResultWidget = new DynamicSyncedWidget<>();

            if (rl.isWaiting()) {
                var reason = rl.getWaitingReason();
                // recipeActionResultWidget.
                // recipeActionResultWidget = rl
            }
        }
        return new EmptyWidget();
    }
}
