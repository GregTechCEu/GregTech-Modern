package com.gregtechceu.gtceu.common.mui.factory;

import com.gregtechceu.gtceu.api.capability.IMonitorComponent;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.IPanelHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.factory.PanelFactory;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widgets.ButtonWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ListWidget;
import com.gregtechceu.gtceu.api.mui.widgets.TextWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Grid;
import com.gregtechceu.gtceu.api.mui.widgets.textfield.TextFieldWidget;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CentralMonitorUIFactory implements PanelFactory {

    public static final CentralMonitorUIFactory INSTANCE = new CentralMonitorUIFactory();

    @Override
    public ModularPanel buildUIFunction(PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                        MetaMachine metaMachine) {
        if (!(metaMachine instanceof CentralMonitorMachine machine)) return new ModularPanel("main");
        return new ModularPanel("main")
                .padding(10)
                .child(new Flow(GuiAxis.Y)
                        .heightRel(.8f)
                        .widthRel(.8f)
                        .child(new Flow(GuiAxis.X)
                                .child(new TextWidget<>(IKey.lang("gtceu.central_monitor.gui.monitor_groups"))
                                        .alignX(0))
                                .child(new ButtonWidget<>()
                                        .alignX(1)
                                        .background(GTGuiTextures.MC_BUTTON, GTGuiTextures.ADD)
                                        .hoverBackground(GTGuiTextures.MC_BUTTON_HOVERED, GTGuiTextures.ADD))
                                .widthRel(1).height(20))
                        .child(new ListWidget<>().children(
                                machine.getMonitorGroups()
                                        .stream()
                                        .map(group -> {
                                            IPanelHandler panelHandler = syncManager.panel(
                                                    "editor_" + group.getName(),
                                                    (syncManager1, panelHandler1) -> this.createGroupEditorPanel(
                                                            syncManager1, panelHandler1,
                                                            machine, group
                                                    ), true
                                            );
                                            return new Flow(GuiAxis.X)
                                                .height(20)
                                                .child(new TextWidget<>(group.getName()))
                                                .child(new ButtonWidget<>()
                                                        .alignX(1)
                                                        .background(GTGuiTextures.MC_BUTTON, GTGuiTextures.EDIT)
                                                        .hoverBackground(GTGuiTextures.MC_BUTTON_HOVERED,
                                                                GTGuiTextures.EDIT)
                                                        .onMousePressed((mouseX, mouseY, button) -> {
                                                            panelHandler.openPanel();
                                                            return true;
                                                        }));
                                        })
                                        .collect(Collectors.toUnmodifiableList()))
                                .widthRel(1).heightRel(.8f)));
    }

    private ModularPanel createGroupEditorPanel(PanelSyncManager syncManager, IPanelHandler panelHandler, CentralMonitorMachine machine, MonitorGroup group) {
        List<List<IWidget>> matrix = new ArrayList<>();
        for (int row = 0; row <= machine.getDownDist() + machine.getUpDist(); row++) {
            List<IWidget> curRow = new ArrayList<>();
            matrix.add(curRow);
            for (int col = 0; col <= machine.getLeftDist() + machine.getRightDist(); col++) {
                IMonitorComponent component = machine.getComponent(row, col);
                IDrawable texture = component == null ? GTGuiTextures.CROSS : component.getIcon();
                curRow.add(new ButtonWidget<>().background(texture));
            }
        }
        return new ModularPanel("group_editor_" + group.getName())
                .padding(10)
                .child(Flow.column()
                        .child(new TextWidget<>(IKey.lang("gtceu.central_monitor.gui.group_editor")))
                        .child(Flow.row()
                                .child(new TextWidget<>(IKey.lang("gtceu.central_monitor.gui.group_name")))
                                .child(new TextFieldWidget()
                                        .padding(4)
                                        .value(SyncHandlers.string(group::getName, group::setName)))))
                .child(new Grid().matrix(matrix));
    }
}
