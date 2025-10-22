package com.gregtechceu.gtceu.common.data.mui;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.mui.drawable.ItemDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;
import com.gregtechceu.gtceu.api.mui.drawable.text.StringKey;
import com.gregtechceu.gtceu.api.mui.factory.PanelEditor;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.utils.WidgetUtil;
import com.gregtechceu.gtceu.api.mui.value.BoolValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.*;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

public class GTMuiEditors {

    public static PanelEditor TEST_EDITOR_1 = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                               MetaMachine machine, ModularPanel panel) -> {
        panel.child(new TextWidget<>(new StringKey("Edit Test 1")).color(0x00ff00).bottom(7).left(4)
                .alignment(Alignment.BottomRight));
    };
    public static PanelEditor TEST_EDITOR_2 = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                               MetaMachine machine, ModularPanel panel) -> {
        panel.child(
                new TextWidget<>(new StringKey("Edit Test 2")).top(40).color(0x0000ff).alignment(Alignment.TopRight));
    };

    public static PanelEditor CHARGE_SLOT = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                             MetaMachine machine, ModularPanel panel) -> {

        if (machine instanceof SimpleTieredMachine simpleTieredMachine) {
            var uiRow = WidgetUtil.getWidget(panel, "Main Ui");
            if(!(uiRow instanceof Row mainUi)){
                return;
            }
            var energyContainer = simpleTieredMachine.getChargerInventory();
            mainUi.child( new ItemSlot().
                    slot(new ModularSlot(energyContainer, 0))
                    .align(Alignment.BottomCenter)).paddingBottom(5);
        }
    };

    public static PanelEditor TITLE =
            (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
             MetaMachine machine, ModularPanel panel) -> {


            if(machine instanceof SimpleTieredMachine simpleTieredMachine) {
                var uiRow = WidgetUtil.getWidget(panel, "Base Panel");
                if (!(uiRow instanceof Column mainPanel)) {
                    return;
                }
                var displayItem = simpleTieredMachine.getDefinition().asStack();
                String name = displayItem.getHoverName().getString();
                name = name.replaceAll("\\s+(§.)", "§");
                mainPanel.child(new Row()
                        .background(GTGuiTextures.BACKGROUND_TITLE)
                        .coverChildren()
                        .bottomRelAnchor(1.35f,0)
                        .name("title row")
                        .child(new ItemDrawable(displayItem)
                                .asIcon()
                                .size(14).asWidget().verticalCenter())
                        .child(new TextWidget<>(name)).paddingRight(4));
            }
    };

    public static PanelEditor PROGRESS_BAR(UITexture texture, int imageSize, ProgressWidget.Direction direction) {
        return (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                MetaMachine machine, ModularPanel panel) -> {
            var slotColumn = WidgetUtil.getWidget(panel, "middle column");
            if(!(slotColumn instanceof Column column)){
                return;
            }
            if (machine instanceof SimpleTieredMachine tieredMachine) {
                ProgressWidget progressBar = new ProgressWidget()
                        .texture(texture, imageSize)
                        .direction(direction)
                        .progress(() -> (tieredMachine.getProgress() / (double) tieredMachine.getMaxProgress()));
                column.child(progressBar.align(Alignment.Center));
            }
        };
    }

    public static PanelEditor POWER_BUTTON = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                           MetaMachine machine, ModularPanel panel) -> {
        if(machine instanceof SimpleTieredMachine tieredMachine) {
            var mainRow = WidgetUtil.getWidget(panel, "Main Ui");
            if(!(mainRow instanceof Row mainUI)){
                return;
            }
            mainUI.child(new ToggleButton()
                                    .value(new BoolValue.Dynamic(
                                            () -> tieredMachine.getRecipeLogic().isWorkingEnabled(),
                                            tieredMachine::setWorkingEnabled))
                                    .selectedBackground(GTGuiTextures.BUTTON_POWER[0])
                                    .align(Alignment.BottomLeft)
                            .paddingBottom(5)
                                    .background(GTGuiTextures.BUTTON_POWER[1]));
        }
    };
}
