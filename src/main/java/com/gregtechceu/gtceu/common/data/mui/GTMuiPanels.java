package com.gregtechceu.gtceu.common.data.mui;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.mui.drawable.text.StringKey;
import com.gregtechceu.gtceu.api.mui.factory.PanelFactory;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.TextWidget;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;

public class GTMuiPanels {

    public static PanelFactory TEST_PANEL = (PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                             MetaMachine machine) -> {
        ModularPanel panel = new ModularPanel("test_panel");
        panel.child(new TextWidget(new StringKey("test").color(0xff0000)));
        return panel;
    };
}
