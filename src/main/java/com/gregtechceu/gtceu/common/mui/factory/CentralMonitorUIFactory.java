package com.gregtechceu.gtceu.common.mui.factory;

import com.gregtechceu.gtceu.api.capability.IMonitorComponent;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.IPanelHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.drawable.BorderDrawable;
import com.gregtechceu.gtceu.api.mui.factory.PanelFactory;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widgets.*;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Grid;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.textfield.TextFieldWidget;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.platform.InputConstants;

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
                                                            machine, group),
                                                    true);
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
                                .widthRel(1).heightRel(.8f)))
                .child(SlotGroupWidget.playerInventory(true));
    }

    private ModularPanel createGroupEditorPanel(PanelSyncManager syncManager, IPanelHandler panelHandler,
                                                CentralMonitorMachine machine, MonitorGroup group) {
        List<List<IWidget>> matrix = new ArrayList<>();
        int matrixWidth = 0;
        for (int row = 0; row <= machine.getDownDist() + machine.getUpDist(); row++) {
            List<IWidget> curRow = new ArrayList<>();
            matrix.add(curRow);
            for (int col = 0; col <= machine.getLeftDist() + machine.getRightDist(); col++) {
                IMonitorComponent component = machine.getComponent(row, col);
                IDrawable texture = component == null ? GTGuiTextures.CROSS : component.getIcon();
                IPanelHandler slotDialogHandler = component == null || component.getDataItems() == null ?
                        null : syncManager.panel("slotDialog",
                                (syncManager1, panelHandler1) -> new SimpleDialog<>(
                                        "slot_number_dialog",
                                        slot -> {
                                            group.setTarget(component.getPos());
                                            group.setDataSlot(slot - 1);
                                        },
                                        new TextFieldWidget().setNumbers(1, component.getDataItems().getSlots()),
                                        w -> Integer.parseInt(w.getText()),
                                        IKey.lang("gtceu.central_monitor.gui.data_slot")).setDraggable(true)
                                        .size(160, 80),
                                true);
                curRow.add(new ButtonWidget<>()
                        .margin(1)
                        .background(texture, new BorderDrawable(() -> {
                            if (component == null) return 0;
                            boolean inGroup = group.contains(component.getPos());
                            BlockPos target = group.getTarget(syncManager.getPlayer().level());
                            boolean isTarget = target != null && target.asLong() == component.getPos().asLong();
                            if (inGroup && isTarget) return 0xFFFF00FF;
                            else if (inGroup) return 0xFF0000FF;
                            else if (isTarget) return 0xFFFF0000;
                            else return 0;
                        }, 1))
                        .hoverBackground(texture, new BorderDrawable(0xFFFFFFFF, 1))
                        .onMousePressed((mouseX, mouseY, button) -> {
                            if (component == null) return false;
                            if (button == InputConstants.MOUSE_BUTTON_LEFT) {
                                if (group.contains(component.getPos())) {
                                    group.remove(component.getPos());
                                } else {
                                    group.add(component.getPos());
                                }
                            } else if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
                                if (slotDialogHandler != null) {
                                    slotDialogHandler.openPanel();
                                } else group.setTarget(component.getPos());
                            }
                            return true;
                        }));
            }
            matrixWidth = Math.max(matrixWidth, curRow.size() * 20);
        }
        int matrixHeight = matrix.size() * 20;
        IMonitorModuleItem moduleItem = null;
        ItemStack stack = group.getItemStackHandler().getStackInSlot(0);
        if (stack.getItem() instanceof IComponentItem componentItem) {
            for (IItemComponent component : componentItem.getComponents()) {
                if (component instanceof IMonitorModuleItem monitorModuleItem) {
                    moduleItem = monitorModuleItem;
                    break;
                }
            }
        }
        IMonitorModuleItem finalModuleItem = moduleItem;
        IPanelHandler moduleEditor = moduleItem == null ? null : syncManager.panel(
                "module_editor",
                (syncManager1, panelHandler1) -> finalModuleItem.createModularPanel(stack, machine, group, syncManager1,
                        panelHandler1),
                true);
        return new ModularPanel("group_editor_" + group.getName())
                .width(Math.max(matrixWidth, 150))
                .height(matrixHeight + 60)
                .padding(10)
                .child(Flow.column()
                        .child(new TextWidget<>(IKey.lang("gtceu.central_monitor.gui.group_editor")))
                        .child(Flow.row()
                                .height(20)
                                .child(new TextWidget<>(IKey.lang("gtceu.central_monitor.gui.group_name"))
                                        .paddingRight(4))
                                .child(new TextFieldWidget()
                                        .value(SyncHandlers.string(group::getName, group::setName)))
                                .child(new ItemSlot().slot(group.getItemStackHandler(), 0).name("module_slot"))
                                .child(new ButtonWidget<>()
                                        .background(GTGuiTextures.MC_BUTTON, GTGuiTextures.EDIT)
                                        .hoverBackground(GTGuiTextures.MC_BUTTON_HOVERED, GTGuiTextures.EDIT)
                                        .setEnabledIf(w -> !group.getItemStackHandler().getStackInSlot(0).isEmpty())
                                        .onMousePressed((mouseX, mouseY, button) -> {
                                            if (moduleEditor != null) moduleEditor.openPanel();
                                            return moduleEditor != null;
                                        })))
                        .child(new Grid().matrix(matrix).alignX(Alignment.CENTER).size(matrixWidth, matrixHeight)));
    }
}
