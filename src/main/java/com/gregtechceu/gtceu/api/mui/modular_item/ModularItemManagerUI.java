package com.gregtechceu.gtceu.api.mui.modular_item;

import brachy.modularui.factory.PlayerInventoryGuiData;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModuleSlot;
import com.gregtechceu.gtceu.api.mui.GTGuiScreen;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.IUIHolder;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.*;
import brachy.modularui.widgets.*;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.layout.Grid;

import java.util.List;
import java.util.Objects;

public class ModularItemManagerUI implements IUIHolder<PlayerInventoryGuiData<?>> {

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        ItemStack stack = Objects.requireNonNull(data.getUsedItemStack());
        IModularItem modularItem = Objects.requireNonNull(GTCapabilityHelper.getModularItem(stack));
        List<ItemModuleSlot> slots = modularItem.getSlots();

        Flow moduleList = Flow.col()
                .coverChildren()
                .childPadding(2)
                .child(Flow.row().coverChildren()
                            .childPadding(5)
                            .child(new ItemDisplayWidget().item(stack))
                            .child(new TextWidget<>(Text.dynamic(stack::getHoverName))))
                .child(new Grid()
                        .gridOfSizeWidth(slots.size(), 2, (x, y, index) -> {
                            AppliedItemModule appliedModule = modularItem.getModuleInSlot(index);
                            if (appliedModule == null) {
                                ButtonWidget<?> button = new ButtonWidget<>()
                                        .height(20)
                                        .width(150)
                                        .overlay(Text.lang("metaarmor.tooltip.modifier.empty"));
                                if (index < slots.size()) {
                                    button.backgroundOverlay(slots.get(index).getSlotTexture());
                                }
                                return button;
                            } else {
                                ItemModule module = appliedModule.getModule();
                                IPanelHandler panelHandler = syncManager.syncedPanel("module" + index, false,
                                        (psm1, handler) -> createPanelForModule(psm1, modularItem, index));
                                return new ButtonWidget<>()
                                        .onMousePressed((ctx, button) -> {
                                            panelHandler.togglePanel();
                                            return false;
                                        })
                                        .height(20)
                                        .width(150)
                                        .overlay(Text.of(module.getDisplayName(appliedModule)))
                                        .backgroundOverlay(slots.get(index).getSlotTexture())
                                        .tooltipDynamic(tooltip -> tooltip.add(module.getInfo()));
                            }}));

        return new ModularPanel<>("modularItem")
                .horizontalCenter()
                .coverChildren()
                .padding(6)
                .child(GTMuiWidgets.createTitleBar(null, "Modules", 250,
                        GTGuiTextures.BACKGROUND))
                .child(moduleList);
    }

    private ModularPanel<?> createPanelForModule(PanelSyncManager psm, IModularItem modularItem, int index) {
        AppliedItemModule appliedModule = Objects.requireNonNull(modularItem.getModuleInSlot(index));
        ItemModule module = appliedModule.getModule();
        ItemStack moduleItem = appliedModule.getModuleItem();

        return new ModularPanel<>("module" + index)
                .coverChildren()
                .child(Flow.col()
                        .margin(4)
                        .coverChildren()
                        .crossAxisAlignment(Alignment.CrossAxis.START)
                        .childIf(moduleItem == null || moduleItem.isEmpty(),
                                () -> new TextWidget<>(Text.dynamic(() -> module.getDisplayName(appliedModule)))
                                        .scale(0.75f)
                                        .horizontalCenter()
                                        .paddingBottom(2))
                        .childIf(moduleItem != null && !moduleItem.isEmpty(), () -> {
                            assert moduleItem != null;
                            return Flow.row()
                                    .coverChildren()
                                    .padding(4)
                                    .paddingLeft(0)
                                    .childPadding(4)
                                    .child(new ItemDisplayWidget().item(moduleItem))
                                    .child(Flow.col()
                                            .coverChildrenWidth()
                                            .childPadding(2)
                                            .heightRel(1)
                                            .child(new TextWidget<>(
                                                    Text.dynamic(() -> module.getDisplayName(appliedModule)))
                                                    .scale(0.75f)
                                                    .left(0))
                                            .child(new TextWidget<>(Text.dynamic(moduleItem::getHoverName))
                                                    .scale(0.6f)
                                                    .left(0)));
                        })
                        .children(module.getSettings(appliedModule, psm, index)));
    }

    @Override
    public ModularScreen createScreen(PlayerInventoryGuiData<?> data, ModularPanel<?> mainPanel) {
        return new GTGuiScreen(mainPanel);
    }
}
