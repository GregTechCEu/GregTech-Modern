package com.gregtechceu.gtceu.api.mui.modular_item;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.*;
import com.gregtechceu.gtceu.api.mui.GTGuiScreen;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.drawable.BorderDrawable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.IUIHolder;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.DynamicDrawable;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.*;
import brachy.modularui.widgets.*;
import brachy.modularui.widgets.layout.Flow;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ModularArmorManagerUI implements IUIHolder<ModularArmorUIFactory.ArmorGuiData> {

    @Override
    public ModularPanel<?> buildUI(ModularArmorUIFactory.ArmorGuiData data, PanelSyncManager syncManager,
                                   UISettings settings) {
        ModularPanel<?> panel = new ModularPanel<>("modularArmor")
                .size(284, 224)
                .background(GTGuiTextures.BACKGROUND);
        PagedWidget<?> pages = new PagedWidget<>().pos(52, 8).size(224, 208);
        pages.addPage(new TextWidget<>(Text.lang("gui.gtceu.armor_config.select"))
                .width(216).horizontalCenter().verticalCenter());
        List<IPanelHandler> popouts = new ArrayList<>();
        Flow armorList = Flow.col().pos(8, 8).coverChildren().childPadding(6);
        String[] labels = { "helmet", "chestplate", "leggings", "boots" };
        for (int armorIndex = 0; armorIndex < data.armor.length; armorIndex++) {
            int pageIndex = armorIndex + 1;
            ItemStack stack = data.armor[armorIndex];
            IModularItem modular = GTCapabilityHelper.getModularItem(stack);
            ButtonWidget<?> button = new ButtonWidget<>().size(36)
                    .overlay(stack.isEmpty() ? Text.str("-") : new ItemDrawable(stack).asIcon().size(20))
                    .tooltipDynamic(tooltip -> {
                        tooltip.add(Component.translatable("gui.gtceu.armor_config." + labels[pageIndex - 1]));
                        if (!stack.isEmpty()) tooltip.add(stack.getHoverName());
                    })
                    .onMousePressed((ctx, mouseButton) -> {
                        if (mouseButton != 0 || modular == null) return false;
                        popouts.forEach(IPanelHandler::closePanel);
                        pages.setPage(pageIndex);
                        return true;
                    });
            button.playClickSound(modular != null);
            button.backgroundOverlay(new DynamicDrawable(
                    () -> new BorderDrawable(pages.getCurrentPageIndex() == pageIndex ? 0xFFFFFFFF : 0x00000000, 1)));
            armorList.child(button);
            Flow moduleList = Flow.col().size(224, 208).childPadding(2);
            moduleList.child(new TextWidget<>(Text.dynamic(stack::getHoverName))
                    .width(224).height(22).scale(0.75f));
            if (modular != null) {
                for (int slotIndex = 0; slotIndex < modular.getSlots().size(); slotIndex++) {
                    ModuleContext applied = modular.getModuleContextForSlot(slotIndex);
                    if (applied == null) continue;
                    int index = slotIndex;
                    String name = "armor" + armorIndex + "_module" + index;
                    IPanelHandler handler = syncManager.syncedPanel(name, true,
                            (psm, h) -> createPanelForModule(psm, modular, index, pageIndex * 100 + index, name));
                    popouts.add(handler);
                    moduleList.child(new ButtonWidget<>().size(224, 16)
                            .overlay(Text.dynamic(() -> applied.getModule().getDisplayName(applied)).scale(0.75f))
                            .tooltipDynamic(tooltip -> GTMuiWidgets.addWrappedModuleDescription(tooltip, applied.getModule().getInfo()))
                            .onMousePressed((ctx, mouseButton) -> {
                                if (mouseButton != 0) return false;
                                for (IPanelHandler other : popouts) {
                                    if (other != handler) other.closePanel();
                                }
                                handler.togglePanel();
                                return true;
                            }));
                }
                if (modular.getAllModuleInstances().isEmpty()) {
                    moduleList.child(new TextWidget<>(Text.lang("gui.gtceu.armor_config.no_modules")));
                }
            }
            pages.addPage(moduleList);
        }
        return panel.child(GTMuiWidgets.createTitleBar(null,
                Component.translatable("gui.gtceu.armor_config.title").getString(), 284,
                GTGuiTextures.BACKGROUND)).child(armorList).child(pages);
    }

    private ModularPanel<?> createPanelForModule(PanelSyncManager psm, IModularItem modularItem, int index,
                                                 int settingsId,
                                                 String name) {
        ModuleContext initial = Objects.requireNonNull(modularItem.getModuleContextForSlot(index));
        ModuleContext appliedModule = new ModuleContext(initial.getAppliedTo(), modularItem, initial.getData()) {

            @Override
            public ModuleData getData() {
                return Objects.requireNonNull(modularItem.getData().getModuleDataForSlot(index));
            }

            @Override
            public <T extends ModuleData> T getData(Class<T> dataClass) {
                return dataClass.cast(getData());
            }
        };
        ItemModule module = appliedModule.getModule();
        ItemStack moduleItem = appliedModule.getData().getModuleItem();

        return new ModularPanel<>(name)
                .background(GTGuiTextures.BACKGROUND)
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
                        .children(module.getSettings(appliedModule, psm, settingsId)));
    }

    @Override
    public ModularScreen createScreen(ModularArmorUIFactory.ArmorGuiData data, ModularPanel<?> mainPanel) {
        return new GTGuiScreen(mainPanel);
    }
}
