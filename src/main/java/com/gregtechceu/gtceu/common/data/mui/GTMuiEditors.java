package com.gregtechceu.gtceu.common.data.mui;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.ItemDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;
import com.gregtechceu.gtceu.api.mui.drawable.text.StringKey;
import com.gregtechceu.gtceu.api.mui.drawable.text.TextRenderer;
import com.gregtechceu.gtceu.api.mui.factory.PanelEditor;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.utils.WidgetUtil;
import com.gregtechceu.gtceu.api.mui.value.BoolValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.WidgetTree;
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
            var mainRow = WidgetTree.findFirstWithNameNullable(panel, "MainUI", Row.class);
            if (mainRow == null) {
                return;
            }
            var energyContainer = simpleTieredMachine.getChargerInventory();
            mainRow.child(new ItemSlot().
                    slot(new ModularSlot(energyContainer, 0))
                    .align(Alignment.BottomCenter));
        }
    };

    public static PanelEditor TITLE = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                       MetaMachine machine, ModularPanel panel) -> {
            if(machine instanceof SimpleTieredMachine simpleTieredMachine) {

                var displayItem = simpleTieredMachine.getDefinition().asStack();
                String name = displayItem.getHoverName().getString();
                name = name.replaceAll("§.", "").trim();
                int borderRadius = 5;
                int minWidth = 149 - 16 - (borderRadius * 2);
                int iconSize = 16;
                int titleWidth = TextRenderer.getFont().width(name) + iconSize + (borderRadius * 2);
                int widgetWidth = Math.min(minWidth, titleWidth);
                int rows = (int) Math.ceil((double) titleWidth / minWidth);
                int heightPerRow = (int) (IKey.renderer.getFontHeight());
                int height = heightPerRow * rows + borderRadius;
                panel.child(new Row()
                        .coverChildrenHeight()
                        .mainAxisAlignment(Alignment.MainAxis.CENTER)
                        .widthRel(.8f)
                        .top(-height - borderRadius)
                        .rightRel(0.5f)
                        .background(GTGuiTextures.BACKGROUND_TITLE)
                        .child(new ItemDrawable(displayItem)
                                .asIcon().size(iconSize)
                                .asWidget()
                                .marginLeft(borderRadius))
                                .mainAxisAlignment(Alignment.MainAxis.START)
                        .child(IKey.str(name)
                                .asWidget()
                                .paddingTop(1)
                                .margin(borderRadius, borderRadius, borderRadius,1)
                                .size(widgetWidth - height, height))
                );


            }
    };

    public static PanelEditor PROGRESS_BAR(UITexture texture, int imageSize, ProgressWidget.Direction direction) {
        return (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                MetaMachine machine, ModularPanel panel) -> {

            var progressColumn = WidgetTree.findFirstWithNameNullable(panel, "progress", Column.class);
            if (progressColumn == null) {
                return;
            }

            if (machine instanceof SimpleTieredMachine tieredMachine) {
                ProgressWidget progressBar = new ProgressWidget()
                        .texture(texture, imageSize)
                        .direction(direction)
                        .progress(() -> (tieredMachine.getProgress() / (double) tieredMachine.getMaxProgress()));
                progressColumn.child(progressBar.align(Alignment.Center));
            }
        };
    }

    public static PanelEditor POWER_BUTTON = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                           MetaMachine machine, ModularPanel panel) -> {
        if(machine instanceof SimpleTieredMachine tieredMachine) {
            var mainRow = WidgetTree.findFirstWithNameNullable(panel, "MainUI", Row.class);
            if (mainRow == null) {
                return;
            }
            mainRow.child(new ToggleButton()
                        .value(new BoolValue.Dynamic(() -> tieredMachine.getRecipeLogic().isWorkingEnabled(),
                                            tieredMachine::setWorkingEnabled))
                        .selectedBackground(GTGuiTextures.BUTTON_POWER[0])
                        .align(Alignment.BottomLeft)
                        .background(GTGuiTextures.BUTTON_POWER[1]));
        }
    };

    public static PanelEditor GT_LOGO = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
    MetaMachine machine, ModularPanel panel) -> {

        if(machine instanceof SimpleTieredMachine tieredMachine) {
            var mainRow = WidgetTree.findFirstWithNameNullable(panel, "MainUI", Row.class);
            if (mainRow == null) {
                return;
            }


        }
    };
}
