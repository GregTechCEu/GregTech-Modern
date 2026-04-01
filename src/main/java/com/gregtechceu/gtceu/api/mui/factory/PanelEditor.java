package com.gregtechceu.gtceu.api.mui.factory;

import brachy.modularui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;

@FunctionalInterface
public interface PanelEditor {

    void editUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                MetaMachine machine, ModularPanel<?> panel);
}
