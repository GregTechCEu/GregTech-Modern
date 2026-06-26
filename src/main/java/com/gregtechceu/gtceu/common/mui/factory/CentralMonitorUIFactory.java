package com.gregtechceu.gtceu.common.mui.factory;

import com.gregtechceu.gtceu.api.capability.IMonitorComponent;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.mui.factory.PanelFactory;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.drawable.BorderDrawable;
import com.gregtechceu.gtceu.common.mui.widgets.SimpleDialog;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.GuiAxis;
import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.value.IValue;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.sync.*;
import brachy.modularui.widgets.*;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntSupplier;

import static com.gregtechceu.gtceu.common.mui.GTByteBufAdapters.MONITOR_GROUPS;

public class CentralMonitorUIFactory implements PanelFactory {

    public static final CentralMonitorUIFactory INSTANCE = new CentralMonitorUIFactory();

    @Override
    public ModularPanel<?> buildUIFunction(PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                           MetaMachine metaMachine) {
        if (!(metaMachine instanceof CentralMonitorMachine machine)) return new ModularPanel<>("main");
        // avoid .allowC2S() here since that will allow unchecked item stack modifying from client-side
        GenericListSyncHandler<FriendlyByteBuf, MonitorGroup> groupSync = new GenericListSyncHandler<>(
                machine::getMonitorGroups,
                machine::setMonitorGroups, MONITOR_GROUPS, MONITOR_GROUPS,
                MONITOR_GROUPS, null);
        syncManager.syncValue("monitor_groups_sync", groupSync);
        List<MonitorGroup> groups = new ArrayList<>(machine.getMonitorGroups());
        IPanelHandler helpPanel = syncManager.syncedPanel(
                "help_panel", true,
                (syncManager1, panelHandler1) -> createHelpPanel());
        IPanelHandler inventoryPanel = syncManager.syncedPanel(
                "inventory", true,
                this::createInventoryPanel);
        Function<MonitorGroup, IWidget> processGroupItem = group -> {
            int index = groupSync.getValue().indexOf(group);
            IPanelHandler moduleEditor = createModulePanelHandler(
                    syncManager,
                    group.getItemStackHandler().getStackInSlot(0),
                    group, machine);
            IPanelHandler panelHandler = syncManager.syncedPanel(
                    "editor_%d".formatted(index), true,
                    (syncManager1, panelHandler1) -> this.createGroupEditorPanel(
                            syncManager1, groupSync, index, machine, moduleEditor));
            return Flow.row()
                    .background(new BorderDrawable(0xFF888888, 1))
                    .height(20)
                    .child(new TextWidget<>(Text.dynamic(() -> Component.literal(group.getName())))
                            .paddingLeft(5)
                            .widthRelOffset(1, -18 * 3))
                    .child(new ItemSlot()
                            .syncHandler(syncManager.getOrCreateSyncHandler(
                                    "module_slot", index,
                                    ItemSlotSyncHandler.class,
                                    () -> new ItemSlotSyncHandler(new ModularSlot(group.getItemStackHandler(), 0)
                                            .changeListener((newItem, onlyAmountChanged, client, init) -> {
                                                if (!init) {
                                                    groups.set(index, group);
                                                    groupSync.setValue(groups, true, false);
                                                }
                                            })))))
                    .child(new ButtonWidget<>()
                            .overlay(GuiTextures.EDIT)
                            .onMousePressed((context, button) -> {
                                panelHandler.openPanel();
                                return true;
                            }))
                    .child(new ButtonWidget<>()
                            .overlay(GuiTextures.REMOVE)
                            .syncHandler(syncManager.getOrCreateSyncHandler("delete_group", index,
                                    InteractionSyncHandler.class, () -> new InteractionSyncHandler()
                                            .setOnMousePressed(mouseData -> {
                                                groups.remove(index);
                                                groupSync.setValue(groups, true, false);
                                            }))))
                    .childIf(moduleEditor != null, () -> new ButtonWidget<>()
                            .right(36)
                            .bottom(1)
                            .background()
                            .hoverBackground()
                            .overlay(GuiTextures.MC_BUTTON, GuiTextures.EDIT)
                            .hoverOverlay(GuiTextures.MC_BUTTON_HOVERED, GuiTextures.EDIT)
                            .size(8)
                            .onMousePressed((ctx, button) -> {
                                assert moduleEditor != null;
                                moduleEditor.openPanel();
                                return true;
                            }));
        };
        DynamicLinkedSyncHandler<FriendlyByteBuf, GenericListSyncHandler<FriendlyByteBuf, MonitorGroup>> listHandler = new DynamicLinkedSyncHandler<>(
                groupSync)
                .widgetProvider((psm, list) -> new ListWidget<>()
                        .children(list.getValue().stream()
                                .map(processGroupItem)
                                .toList())
                        .widthRel(1)
                        .fullHeight()
                        .horizontalCenter());
        return new Dialog<>("main")
                .draggable(true)
                .padding(5)
                .excludeAreaInRecipeViewer()
                .child(GTMuiWidgets.createTitleBar(GTMultiMachines.CENTRAL_MONITOR, 176))
                .child(new Flow(GuiAxis.Y)
                        .heightRel(1)
                        .widthRel(1)
                        .padding(2)
                        .child(Flow.row()
                                .child(new TextWidget<>(Text.lang("gtceu.central_monitor.gui.monitor_groups"))
                                        .verticalCenter())
                                .child(new ButtonWidget<>()
                                        .overlay(GuiTextures.HELP)
                                        .right(0)
                                        .onMousePressed((ctx, button) -> {
                                            helpPanel.openPanel();
                                            return true;
                                        }))
                                .child(new ButtonWidget<>()
                                        .overlay(GuiTextures.SERVER)
                                        .right(18)
                                        .onMousePressed((ctx, button) -> {
                                            inventoryPanel.openPanel();
                                            return true;
                                        }))
                                .child(new ButtonWidget<>()
                                        .overlay(GuiTextures.ADD)
                                        .right(36)
                                        .syncHandler(new InteractionSyncHandler()
                                                .setOnMousePressed(mouseData -> {
                                                    MonitorGroup group = new MonitorGroup(getNewGroupName(groupSync));
                                                    groups.add(group);
                                                    groupSync.setValue(groups, true, false);
                                                })))
                                .widthRel(1).height(20))
                        .child(new DynamicSyncedWidget<>()
                                .overlay(new BorderDrawable(0xFF555555, 4))
                                .syncHandler(listHandler)
                                .padding(4)
                                .widthRel(1)
                                .horizontalCenter()
                                .heightRelOffset(1, -24)));
    }

    private ModularPanel<?> createInventoryPanel(PanelSyncManager psm, IPanelHandler panelHandler) {
        return new ModularPanel<>("inventory")
                .bindPlayerInventory()
                .left(30)
                .height(88);
    }

    private ModularPanel<?> createGroupEditorPanel(PanelSyncManager syncManager,
                                                   GenericListSyncHandler<FriendlyByteBuf, MonitorGroup> groupSync,
                                                   int groupIndex,
                                                   CentralMonitorMachine machine,
                                                   IPanelHandler moduleEditor) {
        List<List<IWidget>> matrix = new ArrayList<>();
        int matrixWidth = 0;
        List<MonitorGroup> groups = List.copyOf(groupSync.getValue());
        MonitorGroup group = groups.get(groupIndex);
        for (int row = 0; row <= machine.getDownDist() + machine.getUpDist(); row++) {
            List<IWidget> curRow = new ArrayList<>();
            matrix.add(curRow);
            for (int col = 0; col <= machine.getLeftDist() + machine.getRightDist(); col++) {
                IMonitorComponent component = machine.getComponent(row, col);
                IDrawable texture = component == null ? GuiTextures.CROSS : component.getIcon();
                String id = "%d_%d_%d".formatted(col, row, groupIndex);
                IPanelHandler slotDialogHandler = component == null || component.getDataItems() == null ?
                        null :
                        syncManager.syncedPanel(
                                "slot_dialog_" + id,
                                true,
                                (syncManager1, panelHandler1) -> new SimpleDialog<>(
                                        "slot_number_dialog_" + id,
                                        new TextFieldWidget().setNumbers(1, component.getDataItems().getSlots()),
                                        w -> Integer.parseInt(w.getText()),
                                        Text.lang("gtceu.central_monitor.gui.data_slot")).resultConsumer(slot -> {
                                            group.setDataSlot(slot - 1);
                                            groupSync.setValue(groups, true, false);
                                        }).draggable(true).size(160, 80));
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
                        .background(texture, new BorderDrawable(colorSupplier, 1), Text.dynamic(() -> {
                            if (component == null || component.getDataItems() == null) return Component.empty();
                            BlockPos target = group.getTargetRaw();
                            boolean isTarget = target != null && target.asLong() == component.getBlockPos().asLong();
                            if (isTarget) return Component.literal(String.valueOf(group.getDataSlot() + 1));
                            else return Component.empty();
                        }))
                        .hoverBackground(texture, new BorderDrawable(() -> colorSupplier.getAsInt() | 0x222222, 1))
                        .syncHandler(new InteractionSyncHandler()
                                .setOnMousePressed(mouseData -> {
                                    if (component == null) return;
                                    int button = mouseData.mouseButton();
                                    if (button == InputConstants.MOUSE_BUTTON_LEFT) {
                                        if (!component.isMonitor()) return;
                                        if (group.contains(component.getBlockPos())) {
                                            group.remove(component.getBlockPos());
                                        } else {
                                            group.add(component.getBlockPos());
                                        }
                                    } else if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
                                        group.setTarget(component.getBlockPos());
                                        groupSync.setValue(groups, true, false);
                                        if (slotDialogHandler != null) {
                                            slotDialogHandler.openPanel();
                                        }
                                    }
                                    groupSync.setValue(groups, true, false);
                                })));
            }
            matrixWidth = Math.max(matrixWidth, curRow.size() * 20);
        }
        int matrixHeight = matrix.size() * 20;
        BoolValue moduleChanged = new BoolValue(false);
        return new ModularPanel<>("editor_%d_panel".formatted(groupIndex))
                .width(Math.max(matrixWidth, 150))
                .height(matrixHeight + 60)
                .excludeAreaInRecipeViewer()
                .child(Flow.column()
                        .padding(10)
                        .child(new TextWidget<>(Text.lang("gtceu.central_monitor.gui.group_editor")))
                        .child(Flow.row()
                                .height(20)
                                .child(new TextWidget<>(Text.lang("gtceu.central_monitor.gui.group_name"))
                                        .paddingRight(4))
                                .child(new TextFieldWidget()
                                        .value(SyncHandlers.string(group::getName, s -> {
                                            group.setName(s);
                                            groupSync.setValue(groups, true, false);
                                        }).allowC2S()))
                                .child(new ItemSlot()
                                        .slot(group.getItemStackHandler(), 0)
                                        .name("module_slot")
                                        .slot(new ModularSlot(group.getItemStackHandler(), 0)
                                                .changeListener((item, amount, client, init) -> {
                                                    groupSync.setValue(groups, true, false);
                                                })))
                                .child(new ButtonWidget<>()
                                        .overlay(GuiTextures.EDIT)
                                        .setEnabledIf(w -> !group.getItemStackHandler().getStackInSlot(0).isEmpty())
                                        .addTooltipLine(Text.lang("gtceu.gui.central_monitor.module_editor_button"))
                                        .onMousePressed((context, button) -> {
                                            if (moduleEditor != null && !moduleChanged.getValue())
                                                moduleEditor.openPanel();
                                            return true;
                                        })))
                        .child(new Grid().grid(matrix).leftRel(0.5f).size(matrixWidth, matrixHeight)));
    }

    private ModularPanel<?> createHelpPanel() {
        return new ModularPanel<>("help_panel")
                .excludeAreaInRecipeViewer()
                .width(500)
                .height(300)
                .resizeableOnDrag(true)
                .child(Flow.column()
                        .margin(5)
                        .child(new TextWidget<>(Text.lang("gtceu.gui.central_monitor.help")))
                        .child(Flow.row()
                                .marginTop(10)
                                .height(40)
                                .widthRel(1)
                                .child(IDrawable.of(new BorderDrawable(0xFFFF0000, 1),
                                        GTGuiTextures.MONITOR).asWidget()
                                        .heightRel(1)
                                        .width(40)
                                        .padding(11)
                                        .background(new BorderDrawable(0xFF888888, 1))
                                        .disableHoverBackground())
                                .child(new TextWidget<>(Text.lang("gtceu.gui.central_monitor.in_group"))
                                        .widthRel(.5f)
                                        .heightRel(1)
                                        .padding(5)
                                        .background(new BorderDrawable(0xFF888888, 1))
                                        .disableHoverBackground())
                                .child(new TextWidget<>(Text.lang("gtceu.gui.central_monitor.left_click"))
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
                                .child(new TextWidget<>(Text.lang("gtceu.gui.central_monitor.target"))
                                        .widthRel(.5f)
                                        .heightRel(1)
                                        .padding(5)
                                        .background(new BorderDrawable(0xFF888888, 1))
                                        .disableHoverBackground())
                                .child(new TextWidget<>(Text.lang("gtceu.gui.central_monitor.right_click"))
                                        .padding(5)
                                        .widthRelOffset(.5f, -40)
                                        .heightRel(1)
                                        .background(new BorderDrawable(0xFF888888, 1))
                                        .disableHoverBackground()))
                        .child(Flow.row()
                                .height(40)
                                .widthRel(1)
                                .child(IDrawable.of(new BorderDrawable(0xFFFF00FF, 1),
                                        GTGuiTextures.MONITOR).asWidget()
                                        .heightRel(1)
                                        .width(40)
                                        .padding(11)
                                        .background(new BorderDrawable(0xFF888888, 1))
                                        .disableHoverBackground())
                                .child(new TextWidget<>(Text.lang("gtceu.gui.central_monitor.in_group_and_target"))
                                        .widthRelOffset(1, -40)
                                        .heightRel(1)
                                        .padding(5)
                                        .background(new BorderDrawable(0xFF888888, 1))))
                        .child(Flow.row()
                                .height(40)
                                .widthRel(1)
                                .child(IDrawable.of(new BorderDrawable(0xFF0000FF, 1),
                                        GTGuiTextures.DATA_HATCH, Text.str("7").color(0xFFFFFFFF)).asWidget()
                                        .heightRel(1)
                                        .width(40)
                                        .padding(11)
                                        .background(new BorderDrawable(0xFF888888, 1))
                                        .disableHoverBackground())
                                .child(new TextWidget<>(Text.lang("gtceu.gui.central_monitor.data_hatch_target"))
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
