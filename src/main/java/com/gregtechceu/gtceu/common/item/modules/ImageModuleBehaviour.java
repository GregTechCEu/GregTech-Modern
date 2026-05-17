package com.gregtechceu.gtceu.common.item.modules;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.client.renderer.monitor.IMonitorRenderer;
import com.gregtechceu.gtceu.client.renderer.monitor.MonitorImageRenderer;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ImageModuleBehaviour implements IMonitorModuleItem, IAddInformation {

    @Override
    public IMonitorRenderer getRenderer(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        return new MonitorImageRenderer(getUrl(stack));
    }

    @Override
    public IPanelHandler createModularPanel(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group,
                                            PanelSyncManager syncManager) {
        return syncManager.syncedPanel("image_module_" + group.getName(), true,
                (psm, handler) -> new ModularPanel<>("image_module_editor")
                        .size(200, 50)
                        .child(Flow.column()
                                .marginTop(5)
                                .center()
                                .widthRel(1)
                                .child(new TextWidget<>(Text.lang("gtceu.gui.central_monitor.url")))
                                .child(new TextFieldWidget()
                                        .value(SyncHandlers.string(() -> getUrl(stack), s -> setUrl(stack, s)))
                                        .center()
                                        .widthRel(.8f))));
    }

    @Override
    public String getType() {
        return "image";
    }

    public String getUrl(ItemStack stack) {
        return stack.getOrCreateTag().getString("url");
    }

    public void setUrl(ItemStack stack, String url) {
        stack.getOrCreateTag().putString("url", url);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("gtceu.item.tooltip.image_url", getUrl(stack))
                .withStyle(ChatFormatting.DARK_AQUA));
    }
}
