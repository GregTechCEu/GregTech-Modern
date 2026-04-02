package com.gregtechceu.gtceu.api.mui.factory;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;

@FunctionalInterface
public interface PanelEditor {

    void editUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                MetaMachine machine, ModularPanel<?> panel);
}
