package com.gregtechceu.gtceu.common.mui.factory;

import com.gregtechceu.gtceu.GTCEu;
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
import com.gregtechceu.gtceu.api.mui.value.BoolValue;
import com.gregtechceu.gtceu.api.mui.value.sync.*;
import com.gregtechceu.gtceu.api.mui.widgets.*;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Grid;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.textfield.TextFieldWidget;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.platform.InputConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntSupplier;

import static com.gregtechceu.gtceu.utils.serialization.network.ByteBufAdapters.MONITOR_GROUPS;

public class CentralMonitorUIFactory implements PanelFactory {

    public static final CentralMonitorUIFactory INSTANCE = new CentralMonitorUIFactory();

    @Override
    public ModularPanel buildUIFunction(PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                        MetaMachine metaMachine) {
        if (!(metaMachine instanceof CentralMonitorMachine machine)) return new ModularPanel("main");
        GenericListSyncHandler<MonitorGroup> groupSync = new GenericListSyncHandler<>(machine::getMonitorGroups,
                machine::setMonitorGroups, MONITOR_GROUPS);
        syncManager.syncValue("monitor_groups_sync", groupSync);
        List<MonitorGroup> groups = new ArrayList<>(machine.getMonitorGroups());
        IPanelHandler helpPanel = syncManager.syncedPanel(
                "help_panel", true,
                (syncManager1, panelHandler1) -> createHelpPanel());
        Function<SortableListWidget.Item<MonitorGroup>, SortableListWidget.Item<MonitorGroup>> processGroupItem = item -> {
            IPanelHandler panelHandler = syncManager.syncedPanel(
                    "editor_" + groups.indexOf(item.getWidgetValue()), true,
                    (syncManager1, panelHandler1) -> this.createGroupEditorPanel(
                            syncManager1, groupSync,
                            machine, item.getWidgetValue(),
                            groups, helpPanel));
            return item.child(Flow.row()
                    .height(20)
                    .child(new TextWidget<>(IKey.dynamic(() -> Component.literal(item.getWidgetValue().getName())))
                            .paddingLeft(5)
                            .widthRelOffset(1, -38))
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
        DynamicSyncHandler listHandler = new DynamicSyncHandler()
                .widgetProvider((psm, buf) -> new SortableListWidget<MonitorGroup>()
                        .children(groups.stream()
                                .map(SortableListWidget.Item::new)
                                .map(processGroupItem)
                                .toList())
                        .onChange(groupSync::setValue)
                        .widthRel(1));
        listHandler.notifyUpdate(buf -> {});
        return new Dialog<>("main")
                .setDraggable(true)
                .padding(5)
                .excludeAreaInRecipeViewer()
                .child(GTMuiWidgets.createTitleBar(GTMultiMachines.CENTRAL_MONITOR, 176))
                .child(new Flow(GuiAxis.Y)
                        .heightRel(1)
                        .widthRel(1)
                        .padding(2)
                        .child(new Flow(GuiAxis.X)
                                .child(new TextWidget<>(IKey.lang("gtceu.central_monitor.gui.monitor_groups"))
                                        .alignX(0))
                                .child(new ButtonWidget<>()
                                        .alignX(1)
                                        .background(GTGuiTextures.MC_BUTTON, GTGuiTextures.ADD)
                                        .hoverBackground(GTGuiTextures.MC_BUTTON_HOVERED, GTGuiTextures.ADD)
                                        .syncHandler(new InteractionSyncHandler()
                                                .setOnMousePressed(mouseData -> {
                                                    MonitorGroup group = new MonitorGroup(getNewGroupName(groupSync));
                                                    groups.add(group);
                                                    GTCEu.LOGGER.info("adding group: {} isClient = {}", groups,
                                                            syncManager.isClient());
                                                    groupSync.setValue(groups, true, false);
                                                    listHandler.notifyUpdate(buf -> {});
                                                })))
                                .widthRel(1).height(20))
                        .child(new DynamicSyncedWidget<>()
                                .syncHandler(listHandler)
                                .widthRel(1)
                                .heightRelOffset(() -> 1, -96))
                        .child(SlotGroupWidget.playerInventory(false)));
    }

    private ModularPanel createGroupEditorPanel(PanelSyncManager syncManager,
                                                GenericListSyncHandler<MonitorGroup> groupSync,
                                                CentralMonitorMachine machine, MonitorGroup group,
                                                List<MonitorGroup> groups,
                                                IPanelHandler helpPanel) {
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
                        syncManager.syncedPanel(
                                "slot_dialog_" + finalCol + "_" + finalRow + "_" + groups.indexOf(group),
                                true,
                                (syncManager1, panelHandler1) -> new SimpleDialog<>(
                                        "slot_number_dialog_" + finalCol + "_" + finalRow + "_" + groups.indexOf(group),
                                        slot -> {
                                            group.setTarget(component.getBlockPos());
                                            group.setDataSlot(slot - 1);
                                            groupSync.setValue(groups);
                                        },
                                        new TextFieldWidget().setNumbers(1, component.getDataItems().getSlots()),
                                        w -> {
                                            w.validateText();
                                            return Integer.parseInt(w.getText());
                                        },
                                        IKey.lang("gtceu.central_monitor.gui.data_slot")).setDraggable(true)
                                        .size(160, 80));
                IntSupplier colorSupplier = () -> {
                    if (component == null) return 0;
                    boolean inGroup = group.contains(component.getBlockPos());
                    BlockPos target = group.getTargetRaw();
                    boolean isTarget = target != null && target.asLong() == component.getBlockPos().asLong();
                    if (inGroup && isTarget) return 0xFFFF00FF;
                    else if (inGroup) return 0xFFFF0000;
                    else if (isTarget) return 0xFF0000FF;
                    else return 0;
                };
                curRow.add(new ButtonWidget<>()
                        .margin(1)
                        .background(texture, new BorderDrawable(colorSupplier, 1), IKey.dynamic(() -> {
                            if (component == null || component.getDataItems() == null) return Component.empty();
                            BlockPos target = group.getTargetRaw();
                            boolean isTarget = target != null && target.asLong() == component.getBlockPos().asLong();
                            if (isTarget) return Component.literal(String.valueOf(group.getDataSlot() + 1));
                            else return Component.empty();
                        }))
                        .hoverBackground(texture, new BorderDrawable(() -> colorSupplier.getAsInt() | 0x222222, 1))
                        .onMousePressed((mouseX, mouseY, button) -> {
                            if (component == null) return true;
                            if (button == InputConstants.MOUSE_BUTTON_LEFT) {
                                if (!component.isMonitor()) return true;
                                if (group.contains(component.getBlockPos())) {
                                    group.remove(component.getBlockPos());
                                } else {
                                    group.add(component.getBlockPos());
                                }
                            } else if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
                                if (slotDialogHandler != null) {
                                    slotDialogHandler.openPanel();
                                } else group.setTarget(component.getBlockPos());
                            }
                            groupSync.setValue(groups);
                            return true;
                        }));
            }
            matrixWidth = Math.max(matrixWidth, curRow.size() * 20);
        }
        int matrixHeight = matrix.size() * 20;
        IPanelHandler moduleEditor = createModulePanelHandler(
                syncManager,
                group.getItemStackHandler().getStackInSlot(0),
                group, machine);
        BoolValue moduleChanged = new BoolValue(false);
        return new ModularPanel("editor_" + groups.indexOf(group) + "_panel")
                .width(Math.max(matrixWidth, 150))
                .height(matrixHeight + 60)
                .excludeAreaInRecipeViewer()
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
                                                        moduleChanged.setValue(true);
                                                })))
                                .child(new ButtonWidget<>()
                                        .background(
                                                new DynamicDrawable(() -> moduleChanged.getValue() ?
                                                        GTGuiTextures.MC_BUTTON_DISABLED :
                                                        GTGuiTextures.MC_BUTTON),
                                                GTGuiTextures.EDIT)
                                        .hoverBackground(
                                                new DynamicDrawable(() -> moduleChanged.getValue() ?
                                                        GTGuiTextures.MC_BUTTON_DISABLED :
                                                        GTGuiTextures.MC_BUTTON_HOVERED),
                                                GTGuiTextures.EDIT)
                                        .setEnabledIf(w -> !group.getItemStackHandler().getStackInSlot(0).isEmpty())
                                        .addTooltipLine(IKey.lang(() -> moduleChanged.getValue() ?
                                                "gtceu.gui.central_monitor.module_editor_disabled" :
                                                "gtceu.gui.central_monitor.module_editor_button"))
                                        .onMousePressed((mouseX, mouseY, button) -> {
                                            if (moduleEditor != null && !moduleChanged.getValue())
                                                moduleEditor.openPanel();
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
                .excludeAreaInRecipeViewer()
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
                                .child(new IDrawable.MultiDrawableWidget(new BorderDrawable(0xFFFF0000, 1),
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
                                .child(new IDrawable.MultiDrawableWidget(new BorderDrawable(0xFFFF00FF, 1),
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
                                .child(new IDrawable.MultiDrawableWidget(new BorderDrawable(0xFF0000FF, 1),
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
        return moduleItem == null ? null : finalModuleItem.createModularPanel(stack, machine, group, syncManager);
    }

    private String getNewGroupName(IValue<List<MonitorGroup>> groupSync) {
        return Component.translatable("gtceu.gui.central_monitor.group_default_name", groupSync.getValue().size() + 1)
                .getString();
    }
}
