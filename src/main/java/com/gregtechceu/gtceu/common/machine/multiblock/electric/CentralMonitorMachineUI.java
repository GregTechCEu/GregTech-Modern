package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IMonitorComponent;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.GTStringUtils;

import com.lowdragmc.lowdraglib.gui.texture.*;
import com.lowdragmc.lowdraglib.gui.widget.*;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.items.IItemHandler;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

final class CentralMonitorMachineUI {

    private CentralMonitorMachineUI() {}

    static Object createUIWidget(CentralMonitorMachine machine, Object builderObject) {
        if (!(builderObject instanceof WidgetGroup builder)) {
            return builderObject;
        }
        machine.updateStructureDimensions();
        machine.selectedComponents.clear();

        WidgetGroup main = new WidgetGroup();
        DraggableScrollableWidgetGroup componentSelection = new DraggableScrollableWidgetGroup(0, 10, 200, 110);
        main.addWidget(componentSelection);
        WidgetGroup options = new WidgetGroup(-100, 20, 60, 20);
        WidgetGroup groupConfig = new WidgetGroup(10, 30, 100, 100);
        groupConfig.setVisible(false);

        ButtonWidget infoWidget = new ButtonWidget(200, 10, 20, 20, null);
        infoWidget.setButtonTexture(GuiTextures.INFO_ICON);
        infoWidget.setHoverTooltips(
                GTStringUtils.toImmutable(LangHandler.getSingleOrMultiLang("gtceu.central_monitor.info_tooltip")));
        builder.addWidget(infoWidget);
        List<MonitorGroup> configGroup = new ArrayList<>();
        configGroup.add(null);

        Consumer<MonitorGroup> openGroupConfig = (group) -> {
            configGroup.set(0, group);
            if (group == null) {
                main.setVisible(true);
                groupConfig.setVisible(false);
                return;
            }
            groupConfig.clearAllWidgets();
            groupConfig.addWidget(new LabelWidget(0, 5, () -> {
                String currentName = "";
                if (configGroup.get(0) != null) {
                    currentName = configGroup.get(0).getName();
                }
                return Component.translatable("gtceu.central_monitor.gui.currently_editing", currentName).getString();
            }));
            for (int i = 0; i < 8; i++) {
                SlotWidget slot = new SlotWidget(group.getPlaceholderSlotsHandler(), i, -38, 16 * i + 46);
                slot.setHoverTooltips(GTStringUtils
                        .toImmutable(LangHandler.getMultiLang("gtceu.gui.computer_monitor_cover.slot_tooltip", i + 1)));
                groupConfig.addWidget(slot);
            }
            SlotWidget slot = new SlotWidget(
                    group.getItemStackHandler(), 0,
                    0, 20);
            WidgetGroup itemUI = new WidgetGroup(40, 20, 100, 100);
            Runnable changeListener = () -> {
                if (slot.getLastItem().is(slot.getItem().getItem())) return;
                itemUI.clearAllWidgets();
                if (slot.getItem().getItem() instanceof IComponentItem item) {
                    for (IItemComponent component : item.getComponents()) {
                        if (component instanceof IMonitorModuleItem module &&
                                module.createUIWidget(slot.getItem(), machine, group) instanceof Widget widget) {
                            itemUI.addWidget(widget);
                        }
                    }
                }
            };
            slot.setChangeListener(changeListener);
            changeListener.run();
            groupConfig.addWidget(itemUI);
            groupConfig.addWidget(slot);
            main.setVisible(false);
            groupConfig.setVisible(true);
        };
        builder.addWidget(groupConfig);
        DraggableScrollableWidgetGroup groupList = new DraggableScrollableWidgetGroup(-100, 50, 70, 80);

        List<List<Consumer<Iterator<IMonitorComponent>>>> imageButtons = new ArrayList<>();
        Map<BlockPos, Runnable> rightClickCallbacks = new HashMap<>();
        int[] dataSlot = new int[2];
        dataSlot[0] = 1;
        dataSlot[1] = 9;
        IntInputWidget dataSlotInput = new IntInputWidget(120, 20, 60, -20, () -> dataSlot[0],
                n -> dataSlot[0] = Mth.clamp(n, 1, dataSlot[1]));
        dataSlotInput.setVisible(false);
        builder.addWidget(dataSlotInput);

        Consumer<MonitorGroup> addGroupToList = group -> {
            ButtonWidget label = new ButtonWidget(20, groupList.widgets.size() * 15 + 5, 60, 10, null);
            TextTexture text = new TextTexture(group.getName());
            text.setType(TextTexture.TextType.LEFT);
            label.setButtonTexture(text);
            label.setOnPressCallback(click -> {
                group.getMonitorPositions().forEach(pos -> {
                    BlockPos rel = machine.toRelative(pos);
                    if (imageButtons.size() - 1 < rel.getY()) return;
                    if (imageButtons.get(rel.getY()).size() - 1 < rel.getX()) return;
                    imageButtons.get(rel.getY()).get(rel.getX()).accept(null);
                });
                if (group.getTargetRaw() != null) {
                    rightClickCallbacks.getOrDefault(group.getTargetRaw(), () -> {}).run();
                }
            });
            groupList.addWidget(label);

            ButtonWidget configButton = new ButtonWidget(
                    0, label.getSelfPositionY() - 3,
                    16, 16,
                    GuiTextures.IO_CONFIG_COVER_SETTINGS,
                    click -> {
                        if (configGroup.get(0) == null) {
                            openGroupConfig.accept(group);
                        } else {
                            openGroupConfig.accept(null);
                        }
                    });
            groupList.addWidget(configButton);
        };

        machine.monitorGroups.forEach(addGroupToList);
        builder.addWidget(groupList);
        main.addWidget(options);
        ButtonWidget removeFromGroupButton = new ButtonWidget(0, 0, 60, 20, null);
        removeFromGroupButton.setButtonTexture(new TextTexture("gtceu.central_monitor.gui.remove_from_group"));
        removeFromGroupButton.setVisible(false);
        ButtonWidget setTargetButton = new ButtonWidget(0, 15, 60, 20, null);
        setTargetButton.setButtonTexture(new TextTexture("gtceu.central_monitor.gui.set_target"));
        setTargetButton.setVisible(false);
        ButtonWidget createGroupButton = new ButtonWidget(0, 0, 60, 20, null);
        createGroupButton.setOnPressCallback(click -> {
            MonitorGroup group = new MonitorGroup(
                    Component.translatable("gtceu.gui.central_monitor.group_default_name",
                            machine.monitorGroups.size() + 1).getString());
            for (IMonitorComponent component : machine.selectedComponents) {
                if (machine.isInAnyGroup(component)) return;
                group.add(component.getBlockPos());
            }
            machine.monitorGroups.add(group);
            addGroupToList.accept(group);

            createGroupButton.setVisible(false);
            removeFromGroupButton.setVisible(true);
            Iterator<IMonitorComponent> it = machine.selectedComponents.iterator();
            while (it.hasNext()) {
                IMonitorComponent c = it.next();
                BlockPos rel = machine.toRelative(c.getBlockPos());
                imageButtons.get(rel.getY()).get(rel.getX()).accept(it);
            }
            if (!machine.selectedTargets.isEmpty()) {
                rightClickCallbacks.getOrDefault(machine.selectedTargets.get(0).getBlockPos(), () -> {}).run();
            }
        });
        setTargetButton.setOnPressCallback(click -> {
            MonitorGroup group = null;
            for (MonitorGroup group2 : machine.monitorGroups) {
                for (IMonitorComponent component : machine.selectedComponents) {
                    if (group2.contains(component.getBlockPos())) {
                        group = group2;
                        break;
                    }
                }
                if (group != null) break;
            }
            if (group == null) return;
            if (machine.selectedTargets.isEmpty()) group.setTarget(null);
            else {
                group.setTarget(machine.selectedTargets.get(0).getBlockPos());
                group.setDataSlot(dataSlot[0] - 1);
            }
        });
        removeFromGroupButton.setOnPressCallback(click -> {
            for (MonitorGroup group : machine.monitorGroups) {
                for (IMonitorComponent component : machine.selectedComponents) group.remove(component.getBlockPos());
            }
            Iterator<MonitorGroup> itg = machine.monitorGroups.iterator();
            while (itg.hasNext()) {
                MonitorGroup group = itg.next();
                if (group.isEmpty()) {
                    group.getItemStackHandler().dropInventoryInWorld(machine.getLevel(), machine.getBlockPos());
                    group.getPlaceholderSlotsHandler().dropInventoryInWorld(machine.getLevel(), machine.getBlockPos());
                    itg.remove();
                }
            }
            groupList.clearAllWidgets();
            machine.monitorGroups.forEach(addGroupToList);

            removeFromGroupButton.setVisible(false);
            createGroupButton.setVisible(true);
            Iterator<IMonitorComponent> it = machine.selectedComponents.iterator();
            while (it.hasNext()) {
                IMonitorComponent c = it.next();
                BlockPos rel = machine.toRelative(c.getBlockPos());
                if (imageButtons.size() - 1 < rel.getY()) continue;
                if (imageButtons.get(rel.getY()).size() - 1 < rel.getX()) continue;
                imageButtons.get(rel.getY()).get(rel.getX()).accept(it);
            }
            if (!machine.selectedTargets.isEmpty()) {
                rightClickCallbacks.getOrDefault(machine.selectedTargets.get(0).getBlockPos(), () -> {}).run();
            }
        });
        createGroupButton.setButtonTexture(new TextTexture("gtceu.central_monitor.gui.create_group"));
        createGroupButton.setVisible(false);
        options.addWidget(removeFromGroupButton);
        options.addWidget(createGroupButton);
        options.addWidget(setTargetButton);
        int startX = 20;
        int startY = 30;
        for (int row = 0; row <= machine.getDownDist() + machine.getUpDist(); row++) {
            imageButtons.add(new ArrayList<>());
            for (int col = 0; col <= machine.getLeftDist() + machine.getRightDist(); col++) {
                IGuiTexture texture = getComponentTexture(machine, row, col);
                GuiTextureGroup textures = new GuiTextureGroup(texture, new ColorBorderTexture(2, 0xFFFFFF));
                IMonitorComponent component = machine.getComponent(row, col);
                if (component == null) {
                    imageButtons.getLast().add(it -> {});
                    continue;
                }
                ButtonWidget img = new ButtonWidget(startX + (16 * col), startY + (16 * row), 16, 16, textures, null);
                Consumer<Iterator<IMonitorComponent>> callback = (it) -> {
                    if (!component.isMonitor()) return;
                    if (machine.selectedComponents.contains(component)) {
                        if (it == null) {
                            machine.selectedComponents.remove(component);
                        } else {
                            it.remove();
                        }

                        if (!machine.selectedTargets.isEmpty() && machine.selectedTargets.get(0) == component) {
                            ColorRectTexture rect = new ColorRectTexture(Color.BLUE);
                            textures.setTextures(rect, texture);
                        } else {
                            textures.setTextures(texture);
                        }

                        createGroupButton.setVisible(
                                machine.selectedComponents.stream().noneMatch(machine::isInAnyGroup));
                        removeFromGroupButton.setVisible(
                                machine.selectedComponents.stream().allMatch(machine::isInAnyGroup));
                        setTargetButton.setVisible(removeFromGroupButton.isVisible());

                        if (machine.selectedComponents.isEmpty()) {
                            createGroupButton.setVisible(false);
                            removeFromGroupButton.setVisible(false);
                            setTargetButton.setVisible(false);
                        }
                    } else {
                        boolean inAnyGroup = machine.isInAnyGroup(component);
                        // yes I know this is terrible but if it works don't touch it :)
                        if (machine.selectedComponents.isEmpty() && !inAnyGroup) createGroupButton.setVisible(true);
                        if (inAnyGroup) createGroupButton.setVisible(false);
                        if (machine.selectedComponents.isEmpty() && inAnyGroup) {
                            removeFromGroupButton.setVisible(true);
                            setTargetButton.setVisible(true);
                        }
                        if (!inAnyGroup) {
                            removeFromGroupButton.setVisible(false);
                            setTargetButton.setVisible(false);
                        }
                        machine.selectedComponents.add(component);
                        ColorRectTexture rect = new ColorRectTexture(
                                (machine.selectedTargets.isEmpty() || machine.selectedTargets.get(0) != component) ?
                                        Color.RED : Color.PINK);
                        textures.setTextures(rect, texture);
                    }
                    if (machine.isInAnyGroup(component)) {
                        machine.monitorGroups.forEach(group -> {
                            if (group.contains(component.getBlockPos())) {
                                img.setHoverTooltips(
                                        Component.translatable("gtceu.gui.central_monitor.group", group.getName()));
                            }
                        });
                    } else {
                        img.setHoverTooltips(Component.translatable("gtceu.gui.central_monitor.group",
                                Component.translatable("gtceu.gui.central_monitor.none")));
                    }
                };
                Runnable rightClickCallback = () -> {
                    if (!machine.selectedTargets.isEmpty()) {
                        if (machine.selectedTargets.get(0).getBlockPos() == component.getBlockPos()) {
                            machine.selectedTargets.clear();
                            if (machine.selectedComponents.contains(component)) {
                                ColorRectTexture rect = new ColorRectTexture(Color.RED);
                                textures.setTextures(rect, texture);
                            } else {
                                textures.setTextures(texture);
                            }
                            dataSlotInput.setVisible(false);
                            return;
                        } else {
                            try {
                                rightClickCallbacks.get(machine.selectedTargets.get(0).getBlockPos()).run();
                            } catch (StackOverflowError e) {
                                GTCEu.LOGGER.error(
                                        "Stack overflow when right-clicking monitor component {} at {} (selectedTarget is {} at {})",
                                        component, component.getBlockPos(), machine.selectedTargets.get(0),
                                        machine.selectedTargets.get(0).getBlockPos());
                            }
                        }
                    }
                    machine.selectedTargets.add(component);
                    ColorRectTexture rect;
                    if (machine.selectedComponents.contains(component)) {
                        rect = new ColorRectTexture(Color.PINK);
                    } else {
                        rect = new ColorRectTexture(Color.BLUE);
                    }
                    textures.setTextures(rect, texture);
                    if (component.getDataItems() != null) {
                        IItemHandler dataItems = component.getDataItems();
                        MonitorGroup selectedGroup = null;
                        for (MonitorGroup group : machine.monitorGroups) {
                            for (IMonitorComponent c : machine.selectedComponents) {
                                if (group.contains(c.getBlockPos())) {
                                    if (selectedGroup == null || selectedGroup == group) {
                                        selectedGroup = group;
                                    } else {
                                        selectedGroup = null;
                                        break;
                                    }
                                }
                            }
                        }
                        if (selectedGroup != null) {
                            dataSlot[0] = selectedGroup.getDataSlot() + 1;
                        }
                        dataSlot[1] = dataItems.getSlots();
                        dataSlotInput.setVisible(true);
                    }
                };
                if (machine.isInAnyGroup(component)) {
                    machine.monitorGroups.forEach(group -> {
                        if (group.contains(component.getBlockPos())) img.setHoverTooltips(
                                Component.translatable("gtceu.gui.central_monitor.group", group.getName()));
                    });
                } else {
                    img.setHoverTooltips(Component.translatable("gtceu.gui.central_monitor.group",
                            Component.translatable("gtceu.gui.central_monitor.none")));
                }
                img.setOnPressCallback(click -> {
                    if (click.button == 0) callback.accept(null);
                    else if (click.button == 1) rightClickCallback.run();
                });
                componentSelection.addWidget(img);
                imageButtons.getLast().add(callback);
                rightClickCallbacks.put(component.getBlockPos(), rightClickCallback);
            }
        }
        builder.addWidget(main);
        return builder;
    }

    private static IGuiTexture getComponentTexture(CentralMonitorMachine machine, int row, int col) {
        if (row < 0 || col < 0 ||
                row > machine.getDownDist() + machine.getUpDist() + 1 ||
                col > machine.getLeftDist() + machine.getRightDist() + 1) {
            return GuiTextures.BLANK_TRANSPARENT;
        }
        IMonitorComponent component = machine.getComponent(row, col);
        if (component == null) return GuiTextures.BLANK_TRANSPARENT;
        Object icon = component.getComponentIcon();
        return icon instanceof IGuiTexture texture ? texture : GuiTextures.BLANK_TRANSPARENT;
    }
}
