package com.gregtechceu.gtceu.common.item.modules;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.api.placeholder.MultiLineComponent;
import com.gregtechceu.gtceu.api.placeholder.PlaceholderContext;
import com.gregtechceu.gtceu.api.placeholder.PlaceholderHandler;
import com.gregtechceu.gtceu.client.renderer.monitor.IMonitorRenderer;
import com.gregtechceu.gtceu.client.renderer.monitor.MonitorTextRenderer;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.value.sync.*;
import org.jetbrains.annotations.Nullable;

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
                stack.getOrCreateTag().getUUID("placeholderUUID"));
    }

    private void updateText(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        if (!stack.getOrCreateTag().contains("placeholderUUID")) {
            stack.getOrCreateTag().putUUID("placeholderUUID", UUID.randomUUID());
        }
        MultiLineComponent text = PlaceholderHandler.processPlaceholders(
                getPlaceholderText(stack), getContext(stack, machine, group));
        stack.getOrCreateTag().put("text",
                text.withStyle(style -> style.withFont(GTGuiTextures.MONOCRAFT_FONT)).toTag());
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
                s -> setPlaceholderText(stack, s));
        DoubleSyncValue scale = SyncHandlers.doubleNumber(
                () -> getScale(stack),
                s -> setScale(stack, s));
        BooleanSyncValue pause = SyncHandlers.bool(() -> isPaused(stack), p -> setPaused(stack, p));
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
        return MultiLineComponent.fromTag(stack.getOrCreateTag().get("text"));
    }

    public double getScale(ItemStack stack) {
        if (!stack.getOrCreateTag().contains("scale"))
            return 1;
        return Math.max(stack.getOrCreateTag().getDouble("scale"), .0001);
    }

    public void setScale(ItemStack stack, double scale) {
        stack.getOrCreateTag().putDouble("scale", scale);
    }

    public void setPaused(ItemStack stack, boolean paused) {
        stack.getOrCreateTag().putBoolean("paused", paused);
    }

    public boolean isPaused(ItemStack stack) {
        if (stack.getOrCreateTag().contains("paused"))
            return stack.getOrCreateTag().getBoolean("paused");
        else return false;
    }

    public void setPlaceholderText(ItemStack stack, String text) {
        ListTag listTag = new ListTag();
        for (String line : text.split("\n")) listTag.add(StringTag.valueOf(line.replaceAll("\r", "")));
        stack.getOrCreateTag().put("formatStringLines", listTag);
    }

    public String getPlaceholderText(ItemStack stack) {
        StringBuilder formatStringLines = new StringBuilder();
        ListTag tag = stack.getOrCreateTag().getList("formatStringLines", StringTag.TAG_STRING);
        for (Tag value : tag) {
            formatStringLines.append(value.getAsString()).append('\n');
        }
        return formatStringLines.toString();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        if (isAdvanced.isAdvanced()) {
            tooltipComponents.add(Component.literal("Placeholder text:").withStyle(ChatFormatting.GOLD));
            tooltipComponents.addAll(MultiLineComponent.literal(getPlaceholderText(stack)));
            tooltipComponents.add(Component.literal("Processed text:").withStyle(ChatFormatting.GOLD));
            tooltipComponents.addAll(getText(stack));
        }
    }
}
