package com.gregtechceu.gtceu.common.data.mui;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.value.sync.BooleanSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.DoubleSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.IntSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.EmptyWidget;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.api.mui.widgets.DynamicSyncedWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ListWidget;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import net.minecraft.network.chat.Component;

public class GTMultiblockPanelUtil {

    private MultiblockControllerMachine controller;

    public GTMultiblockPanelUtil(MultiblockControllerMachine controller) {
        this.controller = controller;
    }

    public Widget<?> getMainTextPanel(PanelSyncManager syncManager) {
        boolean isFormed = controller.isFormed();

        var parentWidget = new ParentWidget<>();
        var listWidget = new ListWidget<>()
                .coverChildrenWidth();
        parentWidget.size(187, 90)
                .child(new IDrawable.DrawableWidget(GTGuiTextures.MUI_DISPLAY).widthRel(1.0f).heightRel(1.0f));

        if (controller instanceof IRecipeLogicMachine rlMachine) {
            //var recipeLogic = rlMachine.getRecipeLogic();

            //boolean isWorking = recipeLogic.isWorking();

            /*var isActiveSync = new BooleanSyncValue(rlMachine.getRecipeLogic()::isActive);
            syncManager.syncValue("isActive", isActiveSync);
            var isWorkingSync = new BooleanSyncValue(rlMachine.getRecipeLogic()::isWorking);
            syncManager.syncValue("isWorking", isWorkingSync);*/
            var currentProgressSync = new IntSyncValue(() -> rlMachine.getRecipeLogic().getProgress(), (i) -> {});
            syncManager.syncValue("currentProgress", currentProgressSync);

            listWidget.child(IKey.dynamic(() -> Component.translatable("gtceu.multiblock.progress",
                                            String.format("%.2f", (float) currentProgressSync.getIntValue()),
                                            String.format("%.2f", (float) 100.0f), 0.0f))
                    //.dynamic(() -> GTMultiblockTextUtil.addProgressLine(controller.isFormed(), isActiveSync.getBoolValue(), currentProgressSync.getIntValue(),
                    //        recipeLogic.getMaxProgress(), recipeLogic.getProgressPercent()))
                    .asWidget().setEnabledIf((r) -> rlMachine.getRecipeLogic().isWorking())
                    );
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
