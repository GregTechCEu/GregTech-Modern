package com.gregtechceu.gtceu.common.mui.factory;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.factory.PanelFactory;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.ButtonWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ListWidget;
import com.gregtechceu.gtceu.api.mui.widgets.TextWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;

import java.util.stream.Collectors;

public class CentralMonitorUIFactory implements PanelFactory {

    public static final CentralMonitorUIFactory INSTANCE = new CentralMonitorUIFactory();

    @Override
    public ModularPanel buildUIFunction(PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                        MetaMachine metaMachine) {
        if (!(metaMachine instanceof CentralMonitorMachine machine)) return null;
        return new ModularPanel("main")
                .padding(10)
                .child(new Flow(GuiAxis.Y)
                        .heightRel(.8f)
                        .widthRel(.8f)
                        .child(new Flow(GuiAxis.X)
                                .child(new TextWidget<>(IKey.lang("gtceu.central_monitor.gui.monitor_groups"))
                                        .alignX(0))
                                .child(new ButtonWidget<>().alignX(1))
                                .widthRel(1).height(20))
                        .child(new ListWidget<>().children(
                                machine.getMonitorGroups()
                                        .stream()
                                        .map(group -> new Flow(GuiAxis.X)
                                                .height(20)
                                                .child(new TextWidget<>(group.getName()))
                                                .child(new ButtonWidget<>().alignX(1)))
                                        .collect(Collectors.toUnmodifiableList()))
                                .widthRel(1).heightRel(.8f)));
    }
}
