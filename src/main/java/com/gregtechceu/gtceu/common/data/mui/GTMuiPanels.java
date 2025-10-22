package com.gregtechceu.gtceu.common.data.mui;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.mui.drawable.text.StringKey;
import com.gregtechceu.gtceu.api.mui.factory.PanelFactory;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.ButtonWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.TextWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

public class GTMuiPanels {

    public static PanelFactory TEST_PANEL = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                             MetaMachine machine) -> {
        ModularPanel panel = new ModularPanel("test_panel");
        panel.child(new TextWidget(new StringKey("test").scale(10.0f).color(0xff0000)));
        return panel;
    };

    public static PanelFactory BASE_PANEL = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
    MetaMachine machine) -> {

        ModularPanel panel = new ModularPanel(machine.getDefinition().getName());
        return panel

                .coverChildren()
                .padding(7)
                .child(Flow
                        .column()
                        .coverChildren()
                        .name("Base Panel")
                        .child(new Row()
                                .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                                .height(80)
                                .name("Main Ui")
                                .child(new Column()
                                        .name("left column")
                                        .coverChildrenWidth()
                                        .crossAxisAlignment(Alignment.CrossAxis.CENTER))
                                .child(new Column()
                                        .coverChildrenWidth()
                                        .name("middle column")
                                        .crossAxisAlignment(Alignment.CrossAxis.CENTER))
                                .child(new Column()
                                       .name("right column")
                                        .coverChildrenWidth()
                                        .crossAxisAlignment(Alignment.CrossAxis.CENTER))
                        )

                        .child(SlotGroupWidget.playerInventory(false ))
                );
    };

}
