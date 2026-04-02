package com.gregtechceu.gtceu.api.machine.trait.feature;

import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.screen.ModularPanel;

public interface IAttachConfiguratorsTrait {

    default void attachLeftConfigurators(Flow flow, ModularPanel panel, PanelSyncManager syncManager) {}

    default void attachRightConfigurators(Flow flow, ModularPanel panel, PanelSyncManager syncManager) {}
}
