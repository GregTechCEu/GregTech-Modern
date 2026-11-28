package com.gregtechceu.gtceu.api.machine.multiblock;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;

import java.util.Collections;
import java.util.List;

@FunctionalInterface
public interface AdditionalDisplayWidgetProvider {

    AdditionalDisplayWidgetProvider DEFAULT = (machine, syncManager) -> Collections.emptyList();

    List<IWidget> getAdditionalWidgets(MultiblockControllerMachine machine, PanelSyncManager syncManager);
}
