package com.gregtechceu.gtceu.common.data.mui;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.drawable.Icon;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.value.sync.*;
import com.gregtechceu.gtceu.api.mui.widget.EmptyWidget;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.api.mui.widgets.DynamicSyncedWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ListWidget;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import net.minecraft.network.chat.Component;

import java.util.Map;

public class GTMultiblockPanelUtil {

    private MultiblockControllerMachine controller;

    public GTMultiblockPanelUtil(MultiblockControllerMachine controller) {
        this.controller = controller;
    }

    public Widget<?> getMainTextPanel(PanelSyncManager syncManager) {
        boolean isFormed = controller.isFormed();

        var parentWidget = new ParentWidget<>();
        var listWidget = new ListWidget<>()
                .widthRel(1f)
                .heightRel(1f)
                .childSeparator(Icon.EMPTY_2PX);
        parentWidget.size(187, 90)
                .child(new IDrawable.DrawableWidget(GTGuiTextures.MUI_DISPLAY).widthRel(1.0f).heightRel(1.0f));

        if (controller instanceof IWorkableMultiController rlMachine) {
            listWidget.child(GTMultiblockTextUtil.addProgressLine(rlMachine, syncManager).alignX(Alignment.CenterLeft));

            if (rlMachine instanceof ITieredMachine tieredMachine) {
                listWidget.child(GTMultiblockTextUtil.addEnergyTierLine(rlMachine, syncManager, tieredMachine.getTier()).alignX(Alignment.CenterLeft));
            }
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
