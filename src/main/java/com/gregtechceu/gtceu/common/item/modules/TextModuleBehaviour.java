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

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TextModuleBehaviour implements IMonitorModuleItem, IAddInformation {

    private void updateText(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        if (!stack.has(GTDataComponents.PLACEHOLDER_UUID)) {
            stack.set(GTDataComponents.PLACEHOLDER_UUID, UUID.randomUUID());
        }
        MultiLineComponent text = PlaceholderHandler.processPlaceholders(
                getPlaceholderText(stack),
                new PlaceholderContext(
                        group.getTargetLevel(machine.getLevel()),
                        group.getTarget(machine.getLevel()),
                        group.getTargetCoverSide(),
                        group.getPlaceholderSlotsHandler(),
                        group.getTargetCover(machine.getLevel()),
                        null,
                        stack.get(GTDataComponents.PLACEHOLDER_UUID)));
        stack.update(GTDataComponents.TEXT_LINE_LIST, TextLineList.EMPTY, lines -> lines.withLines(text.toImmutable()));
    }

    @Override
    public void tick(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        this.updateText(stack, machine, group);
    }

    @Override
    public IMonitorRenderer getRenderer(ItemStack stack) {
        TextLineList lines = stack.getOrDefault(GTDataComponents.TEXT_LINE_LIST, TextLineList.EMPTY);
        return new MonitorTextRenderer(MultiLineComponent.of(lines.lines()), Math.max(lines.scale(), .0001));
    }

    @Override
    public Object createUIWidget(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        return TextModuleBehaviourUI.create(stack, machine, group);
    }

    @Override
    public String getType() {
        return "text";
    }

    public MultiLineComponent getText(ItemStack stack) {
        return MultiLineComponent.of(stack.getOrDefault(GTDataComponents.TEXT_LINE_LIST, TextLineList.EMPTY).lines());
    }

    public float getScale(ItemStack stack) {
        return Math.max(stack.getOrDefault(GTDataComponents.TEXT_LINE_LIST, TextLineList.EMPTY).scale(), .0001f);
    }

    public void setScale(ItemStack stack, float scale) {
        stack.update(GTDataComponents.TEXT_LINE_LIST, TextLineList.EMPTY, lines -> lines.withScale(scale));
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
