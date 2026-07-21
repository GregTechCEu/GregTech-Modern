package com.gregtechceu.gtceu.common.item.modules;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.api.placeholder.MultiLineComponent;
import com.gregtechceu.gtceu.api.placeholder.PlaceholderContext;
import com.gregtechceu.gtceu.api.placeholder.PlaceholderHandler;
import com.gregtechceu.gtceu.client.renderer.monitor.IMonitorRenderer;
import com.gregtechceu.gtceu.client.renderer.monitor.MonitorTextRenderer;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.item.datacomponents.TextLineList;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.value.sync.*;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TextModuleBehaviour implements IMonitorModuleItem, IAddInformation {

    private PlaceholderContext getContext(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        return new PlaceholderContext(
                group.getTargetLevel(machine.getLevel()),
                group.getTarget(machine.getLevel()),
                group.getTargetCoverSide(),
                group.getPlaceholderSlotsHandler(),
                group.getTargetCover(machine.getLevel()),
                group,
                null,
                stack.get(GTDataComponents.PLACEHOLDER_UUID));
    }

    private void updateText(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        if (!stack.has(GTDataComponents.PLACEHOLDER_UUID)) {
            stack.set(GTDataComponents.PLACEHOLDER_UUID, UUID.randomUUID());
        }
        MultiLineComponent text = PlaceholderHandler.processPlaceholders(
                getPlaceholderText(stack),
                getContext(stack, machine, group));
        stack.update(GTDataComponents.TEXT_LINE_LIST, TextLineList.EMPTY, lines -> lines.withLines(text.toImmutable()));
    }

    @Override
    public void tick(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        if (!isPaused(stack))
            this.updateText(stack, machine, group);
    }

    @Override
    public IMonitorRenderer getRenderer(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        return new MonitorTextRenderer(
                getText(stack),
                Math.max(getScale(stack), .0001));
    }

    @Override
    public IPanelHandler createModularPanel(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group,
                                            PanelSyncManager syncManager) {
        PlaceholderContext ctx = getContext(stack, machine, group);
        StringSyncValue code = SyncHandlers.string(
                () -> getPlaceholderText(stack),
                s -> setPlaceholderText(stack, s))
                .allowC2S();
        DoubleSyncValue scale = SyncHandlers.doubleNumber(
                () -> getScale(stack),
                s -> setScale(stack, s))
                .allowC2S();
        BooleanSyncValue pause = SyncHandlers.bool(() -> isPaused(stack), p -> setPaused(stack, p))
                .allowC2S();
        Runnable updateText = () -> updateText(stack, machine, group);
        assert ctx.itemStackHandler() != null;
        return PlaceholderHandler.createPlaceholderEditor("text_module_" + group.getName(), syncManager, ctx, code,
                scale, null, pause,
                updateText);
    }

    @Override
    public String getType() {
        return "text";
    }

    public MultiLineComponent getText(ItemStack stack) {
        return MultiLineComponent.of(stack.getOrDefault(GTDataComponents.TEXT_LINE_LIST, TextLineList.EMPTY).lines());
    }

    public double getScale(ItemStack stack) {
        return Math.max(stack.getOrDefault(GTDataComponents.TEXT_LINE_LIST, TextLineList.EMPTY).scale(), .0001f);
    }

    public void setScale(ItemStack stack, double scale) {
        stack.update(GTDataComponents.TEXT_LINE_LIST, TextLineList.EMPTY, lines -> lines.withScale(scale));
    }

    public void setPaused(ItemStack stack, boolean paused) {
        stack.update(GTDataComponents.TEXT_LINE_LIST, TextLineList.EMPTY, lines -> lines.withPaused(paused));
    }

    public boolean isPaused(ItemStack stack) {
        return stack.getOrDefault(GTDataComponents.TEXT_LINE_LIST, TextLineList.EMPTY).paused();
    }

    public void setPlaceholderText(ItemStack stack, String text) {
        List<Component> lines = Arrays.stream(text.split("\n"))
                .map(Component::literal)
                .map(Component.class::cast)
                .toList();
        stack.update(GTDataComponents.FORMAT_STRING_LIST, TextLineList.EMPTY,
                formatStringList -> formatStringList.withLines(lines));
    }

    public String getPlaceholderText(ItemStack stack) {
        StringBuilder formatStringLines = new StringBuilder();
        List<Component> lines = stack.getOrDefault(GTDataComponents.FORMAT_STRING_LIST, TextLineList.EMPTY).lines();
        for (Component line : lines) {
            formatStringLines.append(line.getString()).append('\n');
        }
        return formatStringLines.toString();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext context,
                                List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        if (isAdvanced.isAdvanced()) {
            tooltipComponents.add(Component.literal("Placeholder text:").withStyle(ChatFormatting.GOLD));
            tooltipComponents
                    .addAll(stack.getOrDefault(GTDataComponents.FORMAT_STRING_LIST, TextLineList.EMPTY).lines());
            tooltipComponents.add(Component.literal("Processed text:").withStyle(ChatFormatting.GOLD));
            tooltipComponents.addAll(getText(stack));
        }
    }
}
