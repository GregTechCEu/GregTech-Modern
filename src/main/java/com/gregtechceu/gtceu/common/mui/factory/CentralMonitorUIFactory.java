package com.gregtechceu.gtceu.common.mui.factory;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IMonitorComponent;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.IPanelHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.value.IValue;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.drawable.BorderDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.DynamicDrawable;
import com.gregtechceu.gtceu.api.mui.factory.PanelFactory;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.sync.GenericSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widgets.*;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Grid;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.textfield.TextFieldWidget;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.serialization.network.ByteBufAdapters;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.platform.InputConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntSupplier;

public class CentralMonitorUIFactory implements PanelFactory {

    public static final CentralMonitorUIFactory INSTANCE = new CentralMonitorUIFactory();

    @Override
    public ModularPanel buildUIFunction(PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                        MetaMachine metaMachine) {
        if (!(metaMachine instanceof CentralMonitorMachine machine)) return new ModularPanel("main");
        GenericSyncValue<List<MonitorGroup>> groupSync = new GenericSyncValue<>(machine::getMonitorGroups,
                machine::setMonitorGroups, ByteBufAdapters.MONITOR_GROUPS);
        syncManager.syncValue("monitor_groups_sync", groupSync);
        List<MonitorGroup> groups = new ArrayList<>(groupSync.getValue());
        SortableListWidget<MonitorGroup> listWidget = new SortableListWidget<>();
        Function<SortableListWidget.Item<MonitorGroup>, SortableListWidget.Item<MonitorGroup>> processGroupItem = item -> {
            IPanelHandler panelHandler = syncManager.panel(
                    "editor_" + groups.indexOf(item.getWidgetValue()),
                    (syncManager1, panelHandler1) -> this.createGroupEditorPanel(
                            syncManager1, groupSync,
                            machine, item.getWidgetValue(), groups),
                    true);
            return item.child(Flow.row()
                    .height(20)
                    .child(new TextWidget<>(IKey.str(() -> item.getWidgetValue().getName()))
                            .paddingLeft(5)
                            .widthRelOffset(1, -36))
                    .child(new ButtonWidget<>()
                            .background(GTGuiTextures.EDIT)
                            .hoverBackground(GTGuiTextures.EDIT, new BorderDrawable())
                            .onMousePressed((mouseX, mouseY, button) -> {
                                panelHandler.openPanel();
                                return true;
                            }))
                    .child(new ButtonWidget<>()
                            .background(GTGuiTextures.CLOSE)
                            .hoverBackground(GTGuiTextures.CLOSE, new BorderDrawable())
                            .onMousePressed((mouseX, mouseY, button) -> {
                                groups.remove(item.getWidgetValue());
                                groupSync.setValue(groups);
                                item.removeSelfFromList();
                                return true;
                            })));
        };
        IPanelHandler newGroupPanelHandler = syncManager.panel(
                "editor_" + groups.size(),
                (syncManager1, panelHandler1) -> {
                    MonitorGroup group = new MonitorGroup(getNewGroupName(groupSync));
                    groups.add(group);
                    listWidget.child(processGroupItem.apply(new SortableListWidget.Item<>(group)));
                    groupSync.setValue(groups, true, false);
                    return this.createGroupEditorPanel(
                            syncManager1, groupSync,
                            machine, group, groups);
                },
                true);
        return new ModularPanel("main")
                .padding(5)
                .child(new Flow(GuiAxis.Y)
                        .heightRel(.8f)
                        .widthRel(.8f)
                        .child(new Flow(GuiAxis.X)
                                .child(new TextWidget<>(IKey.lang("gtceu.central_monitor.gui.monitor_groups"))
                                        .alignX(0))
                                .child(new ButtonWidget<>()
                                        .alignX(1)
                                        .background(GTGuiTextures.MC_BUTTON, GTGuiTextures.ADD)
                                        .hoverBackground(GTGuiTextures.MC_BUTTON_HOVERED, GTGuiTextures.ADD)
                                        .onMousePressed((mouseX, mouseY, button) -> {
                                            newGroupPanelHandler.openPanel();
                                            return true;
                                        }))
                                .widthRel(1).height(20))
                        .child(listWidget.children(
                                groups.stream()
                                        .map(SortableListWidget.Item::new)
                                        .map(processGroupItem)
                                        .toList())
                                .onChange(groupSync::setValue)
                                .widthRel(1).heightRel(.8f)))
                .child(SlotGroupWidget.playerInventory(true));
    }

    private ModularPanel createGroupEditorPanel(PanelSyncManager syncManager,
                                                GenericSyncValue<List<MonitorGroup>> groupSync,
                                                CentralMonitorMachine machine, MonitorGroup group,
                                                List<MonitorGroup> groups) {
        List<List<IWidget>> matrix = new ArrayList<>();
        int matrixWidth = 0;
        for (int row = 0; row <= machine.getDownDist() + machine.getUpDist(); row++) {
            List<IWidget> curRow = new ArrayList<>();
            matrix.add(curRow);
            for (int col = 0; col <= machine.getLeftDist() + machine.getRightDist(); col++) {
                IMonitorComponent component = machine.getComponent(row, col);
                IDrawable texture = component == null ? GTGuiTextures.CROSS : component.getIcon();
                int finalCol = col;
                int finalRow = row;
                IPanelHandler slotDialogHandler = component == null || component.getDataItems() == null ?
                        null :
                        syncManager.panel("slot_dialog_" + finalCol + "_" + finalRow + "_" + groups.indexOf(group),
                                (syncManager1, panelHandler1) -> new SimpleDialog<>(
                                        "slot_number_dialog_" + finalCol + "_" + finalRow + "_" + groups.indexOf(group),
                                        slot -> {
                                            group.setTarget(component.getPos());
                                            group.setDataSlot(slot - 1);
                                            groupSync.setValue(groups);
                                        },
                                        new TextFieldWidget().setNumbers(1, component.getDataItems().getSlots()),
                                        w -> {
                                            w.validateText();
                                            return Integer.parseInt(w.getText());
                                        },
                                        IKey.lang("gtceu.central_monitor.gui.data_slot")).setDraggable(true)
                                        .size(160, 80),
                                true);
                IntSupplier colorSupplier = () -> {
                    if (component == null) return 0;
                    boolean inGroup = group.contains(component.getPos());
                    BlockPos target = group.getTargetRaw();
                    boolean isTarget = target != null && target.asLong() == component.getPos().asLong();
                    if (inGroup && isTarget) return 0xFFFF00FF;
                    else if (inGroup) return 0xFFFF0000;
                    else if (isTarget) return 0xFF0000FF;
                    else return 0;
                };
                curRow.add(new ButtonWidget<>()
                        .margin(1)
                        .background(texture, new BorderDrawable(colorSupplier, 1), IKey.str(() -> {
                            if (component == null || component.getDataItems() == null) return "";
                            BlockPos target = group.getTargetRaw();
                            boolean isTarget = target != null && target.asLong() == component.getPos().asLong();
                            if (isTarget) return String.valueOf(group.getDataSlot() + 1);
                            else return "";
                        }))
                        .hoverBackground(texture, new BorderDrawable(() -> colorSupplier.getAsInt() | 0x222222, 1))
                        .onMousePressed((mouseX, mouseY, button) -> {
                            if (component == null) return true;
                            if (button == InputConstants.MOUSE_BUTTON_LEFT) {
                                if (!component.isMonitor()) return true;
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
                            groupSync.setValue(groups);
                            return true;
                        }));
            }
            matrixWidth = Math.max(matrixWidth, curRow.size() * 20);
        }
        int matrixHeight = matrix.size() * 20;
        IPanelHandler moduleEditor = createModulePanelHandler(syncManager,
                group.getItemStackHandler().getStackInSlot(0), group, machine);
        IPanelHandler helpPanel = syncManager.panel(
                "help_panel_" + groups.indexOf(group),
                (syncManager1, panelHandler1) -> createHelpPanel(),
                true);
        List<Boolean> moduleChanged = new ArrayList<>();
        moduleChanged.add(false);
        return new ModularPanel("editor_" + groups.indexOf(group) + "_panel")
                .width(Math.max(matrixWidth, 150))
                .height(matrixHeight + 60)
                .child(Flow.column()
                        .padding(10)
                        .child(new TextWidget<>(IKey.lang("gtceu.central_monitor.gui.group_editor")))
                        .child(Flow.row()
                                .height(20)
                                .child(new TextWidget<>(IKey.lang("gtceu.central_monitor.gui.group_name"))
                                        .paddingRight(4))
                                .child(new TextFieldWidget()
                                        .value(SyncHandlers.string(group::getName, group::setName)))
                                .child(new ItemSlot()
                                        .slot(group.getItemStackHandler(), 0)
                                        .name("module_slot")
                                        .slot(new ModularSlot(group.getItemStackHandler(), 0)
                                                .changeListener((item, amount, client, init) -> {
                                                    if (!amount && !init)
                                                        moduleChanged.set(0, true);
                                                })))
                                .child(new ButtonWidget<>()
                                        .background(
                                                new DynamicDrawable(() -> moduleChanged.get(0) ?
                                                        GTGuiTextures.MC_BUTTON_DISABLED :
                                                        GTGuiTextures.MC_BUTTON),
                                                GTGuiTextures.EDIT)
                                        .hoverBackground(
                                                new DynamicDrawable(() -> moduleChanged.get(0) ?
                                                        GTGuiTextures.MC_BUTTON_DISABLED :
                                                        GTGuiTextures.MC_BUTTON_HOVERED),
                                                GTGuiTextures.EDIT)
                                        .setEnabledIf(w -> !group.getItemStackHandler().getStackInSlot(0).isEmpty())
                                        .addTooltipLine(IKey.lang(() -> moduleChanged.get(0) ?
                                                "gtceu.gui.central_monitor.module_editor_disabled" :
                                                "gtceu.gui.central_monitor.module_editor_button"))
                                        .onMousePressed((mouseX, mouseY, button) -> {
                                            if (moduleEditor != null && !moduleChanged.get(0)) moduleEditor.openPanel();
                                            return true;
                                        })))
                        .child(new Grid().matrix(matrix).alignX(Alignment.CENTER).size(matrixWidth, matrixHeight)))
                .child(new ButtonWidget<>()
                        .align(Alignment.TopRight)
                        .background(GTGuiTextures.HELP)
                        .hoverBackground(GTGuiTextures.HELP, new BorderDrawable())
                        .onMousePressed((mouseX, mouseY, button) -> {
                            helpPanel.openPanel();
                            return true;
                        }));
    }

    private ModularPanel createHelpPanel() {
        return new ModularPanel("help_panel")
                .width(500)
                .height(300)
                .resizeableOnDrag(true)
                .child(Flow.column()
                        .margin(5)
                        .child(new TextWidget<>(IKey.lang("gtceu.gui.central_monitor.help")))
                        .child(Flow.row()
                                .marginTop(10)
                                .height(40)
                                .widthRel(1)
                                .child(new IDrawable.DrawableWidget(new BorderDrawable(0xFFFF0000, 1),
                                        GTGuiTextures.MONITOR)
                                        .heightRel(1)
                                        .width(40)
                                        .padding(11)
                                        .background(new BorderDrawable(0xFF888888, 1))
                                        .disableHoverBackground())
                                .child(new TextWidget<>(IKey.lang("gtceu.gui.central_monitor.in_group"))
                                        .widthRel(.5f)
                                        .heightRel(1)
                                        .padding(5)
                                        .background(new BorderDrawable(0xFF888888, 1))
                                        .disableHoverBackground())
                                .child(new TextWidget<>(IKey.lang("gtceu.gui.central_monitor.left_click"))
                                        .padding(5)
                                        .widthRelOffset(.5f, -40)
                                        .heightRel(1)
                                        .background(new BorderDrawable(0xFF888888, 1))
                                        .disableHoverBackground()))
                        .child(Flow.row()
                                .height(40)
                                .widthRel(1)
                                .child(new IDrawable.DrawableWidget(new BorderDrawable(0xFF0000FF, 1))
                                        .heightRel(1)
                                        .width(40)
                                        .padding(11)
                                        .background(new BorderDrawable(0xFF888888, 1))
                                        .disableHoverBackground())
                                .child(new TextWidget<>(IKey.lang("gtceu.gui.central_monitor.target"))
                                        .widthRel(.5f)
                                        .heightRel(1)
                                        .padding(5)
                                        .background(new BorderDrawable(0xFF888888, 1))
                                        .disableHoverBackground())
                                .child(new TextWidget<>(IKey.lang("gtceu.gui.central_monitor.right_click"))
                                        .padding(5)
                                        .widthRelOffset(.5f, -40)
                                        .heightRel(1)
                                        .background(new BorderDrawable(0xFF888888, 1))
                                        .disableHoverBackground()))
                        .child(Flow.row()
                                .height(40)
                                .widthRel(1)
                                .child(new IDrawable.DrawableWidget(new BorderDrawable(0xFFFF00FF, 1),
                                        GTGuiTextures.MONITOR)
                                        .heightRel(1)
                                        .width(40)
                                        .padding(11)
                                        .background(new BorderDrawable(0xFF888888, 1))
                                        .disableHoverBackground())
                                .child(new TextWidget<>(IKey.lang("gtceu.gui.central_monitor.in_group_and_target"))
                                        .widthRelOffset(1, -40)
                                        .heightRel(1)
                                        .padding(5)
                                        .background(new BorderDrawable(0xFF888888, 1))))
                        .child(Flow.row()
                                .height(40)
                                .widthRel(1)
                                .child(new IDrawable.DrawableWidget(new BorderDrawable(0xFF0000FF, 1),
                                        GTGuiTextures.DATA_HATCH, IKey.str("7").color(0xFFFFFFFF))
                                        .heightRel(1)
                                        .width(40)
                                        .padding(11)
                                        .background(new BorderDrawable(0xFF888888, 1))
                                        .disableHoverBackground())
                                .child(new TextWidget<>(IKey.lang("gtceu.gui.central_monitor.data_hatch_target"))
                                        .widthRelOffset(1, -40)
                                        .heightRel(1)
                                        .padding(5)
                                        .background(new BorderDrawable(0xFF888888, 1)))));
    }

    private IPanelHandler createModulePanelHandler(PanelSyncManager syncManager, ItemStack stack, MonitorGroup group,
                                                   CentralMonitorMachine machine) {
        IMonitorModuleItem moduleItem = null;
        if (stack.getItem() instanceof IComponentItem componentItem) {
            for (IItemComponent component : componentItem.getComponents()) {
                if (component instanceof IMonitorModuleItem monitorModuleItem) {
                    moduleItem = monitorModuleItem;
                    break;
                }
            }
        }
        IMonitorModuleItem finalModuleItem = moduleItem;
        return moduleItem == null ? null : syncManager.panel(
                "module_editor_" + GTValues.RNG.nextInt(),
                (syncManager1, panelHandler1) -> finalModuleItem.createModularPanel(stack, machine, group, syncManager1,
                        panelHandler1),
                true);
    }

    private String getNewGroupName(IValue<List<MonitorGroup>> groupSync) {
        return Component.translatable("gtceu.gui.central_monitor.group_default_name", groupSync.getValue().size() + 1)
                .getString();
    }
}
