package com.gregtechceu.gtceu.common.data.mui;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;
import com.gregtechceu.gtceu.api.mui.drawable.text.StringKey;
import com.gregtechceu.gtceu.api.mui.factory.PanelEditor;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.ProgressWidget;
import com.gregtechceu.gtceu.api.mui.widgets.TextWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import java.util.function.Supplier;

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
            var energyContainer = simpleTieredMachine.getChargerInventory();
            panel.child(new Column()
                            .widthRel(1.0f)
                            .heightRel(.5f)
                            .paddingBottom(5)
                    .child( new ItemSlot().slot(new ModularSlot(energyContainer, 0))
                    .align(Alignment.BottomCenter)));
        }
    };


    public static PanelEditor PROGRESS_BAR(UITexture texture, int imageSize, ProgressWidget.Direction direction) {
        return (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                MetaMachine machine, ModularPanel panel) -> {
            if (machine instanceof SimpleTieredMachine tieredMachine) {
                ProgressWidget progressBar = new ProgressWidget()
                        .texture(texture, imageSize)
                        .direction(direction)
                        .progress(() -> (tieredMachine.getProgress() / (double) tieredMachine.getMaxProgress()));
                panel.child(new Column()
                        .widthRel(1.0f)
                        .heightRel(.5f)
                        .child(progressBar.align(Alignment.Center))
                );
            }
        };
    }

    public static PanelEditor POWER_BUTTON = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                           MetaMachine machine, ModularPanel panel) -> {




    };
}
