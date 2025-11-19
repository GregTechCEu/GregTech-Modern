package com.gregtechceu.gtceu.common.item.modules;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.api.mui.base.IPanelHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.BorderDrawable;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widgets.*;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.textfield.CodeEditorWidget;
import com.gregtechceu.gtceu.api.mui.widgets.textfield.TextFieldWidget;
import com.gregtechceu.gtceu.api.placeholder.MultiLineComponent;
import com.gregtechceu.gtceu.api.placeholder.PlaceholderContext;
import com.gregtechceu.gtceu.api.placeholder.PlaceholderHandler;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;
import com.gregtechceu.gtceu.client.renderer.monitor.IMonitorRenderer;
import com.gregtechceu.gtceu.client.renderer.monitor.MonitorTextRenderer;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.data.lang.LangHandler;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class TextModuleBehaviour implements IMonitorModuleItem, IAddInformation {

    private void updateText(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        if (!stack.getOrCreateTag().contains("placeholderUUID")) {
            stack.getOrCreateTag().putUUID("placeholderUUID", UUID.randomUUID());
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
                        stack.getOrCreateTag().getUUID("placeholderUUID")));
        stack.getOrCreateTag().put("text", text.toTag());
    }

    @Override
    public void tick(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        if (!isPaused(stack))
            this.updateText(stack, machine, group);
    }

    @Override
    public IMonitorRenderer getRenderer(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        return new MonitorTextRenderer(
                getText(stack).toImmutable(),
                Math.max(getScale(stack), .0001));
    }

    @Override
    public ModularPanel createModularPanel(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group,
                                           PanelSyncManager syncManager, IPanelHandler panelHandler) {
        return new ModularPanel("text_module_editor")
                .size(400, 250)
                .resizeableOnDrag(true)
                .child(Flow.row()
                        .child(Flow.column()
                                .coverChildren()
                                .paddingLeft(4)
                                .children(
                                        group.getPlaceholderSlotsHandler().getSlots(),
                                        i -> new ItemSlot()
                                                .slot(group.getPlaceholderSlotsHandler(), i)
                                                .addTooltipLine(
                                                        IKey.lang("gtceu.gui.computer_monitor_cover.slot_tooltip", i))))
                        .child(Flow.column()
                                .widthRel(.8f)
                                .padding(5)
                                .child(Flow.row()
                                        .height(20)
                                        .child(new TextWidget<>(IKey.str("gtceu.gui.central_monitor.text_scale")))
                                        .child(new TextFieldWidget()
                                                .setNumbersDouble(x -> Math.max(x, 0))
                                                .setDefaultNumber(1.0)
                                                .value(SyncHandlers.string(
                                                        () -> String.valueOf(getScale(stack)),
                                                        s -> setScale(stack, Double.parseDouble(s))))
                                                .marginLeft(4))
                                        .child(new ToggleButton()
                                                .value(SyncHandlers.bool(() -> isPaused(stack),
                                                        p -> setPaused(stack, p)))
                                                .background(false, GTGuiTextures.PAUSE)
                                                .background(true, GTGuiTextures.PLAY)
                                                .addTooltip(false, IKey.lang("gtceu.gui.central_monitor.pause"))
                                                .addTooltip(true, IKey.lang("gtceu.gui.central_monitor.resume"))
                                                .margin(4))
                                        .child(new ButtonWidget<>()
                                                .background(GTGuiTextures.RIGHTLOAD)
                                                .hoverBackground(GTGuiTextures.RIGHTLOAD, new BorderDrawable())
                                                .addTooltipLine(IKey.lang("gtceu.gui.central_monitor.update_once"))
                                                .onMousePressed((mouseX, mouseY, button) -> {
                                                    updateText(stack, machine, group);
                                                    return true;
                                                })))
                                .child(new CodeEditorWidget<>()
                                        .language(PlaceholderHandler.LANG_DEFINITION)
                                        .value(SyncHandlers.string(() -> getPlaceholderText(stack),
                                                s -> setPlaceholderText(stack, s)))
                                        .widthRel(.95f)
                                        .heightRelOffset(() -> 1, -25)))
                        .child(new SortableListWidget<String>()
                                .widthRel(.2f)
                                .paddingBottom(5)
                                .children(PlaceholderHandler.getAllPlaceholderNames()
                                        .stream()
                                        .sorted()
                                        .map(SortableListWidget.Item::new)
                                        .map(w -> w
                                                .child(new TextWidget<>(w.getWidgetValue())
                                                        .sizeRel(1)
                                                        .alignment(Alignment.CENTER))
                                                .tooltip(new RichTooltip()
                                                        .addDrawableLines(LangHandler
                                                                .getSingleOrMultiLang(
                                                                        "gtceu.placeholder_info." + w.getWidgetValue())
                                                                .stream()
                                                                .map(IKey::lang)
                                                                .map(key -> (IDrawable) key)
                                                                .toList())))
                                        .toList())));
    }

    @Override
    public Widget createUIWidget(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        return null;
    }

    @Override
    public String getType() {
        return "text";
    }

    public MultiLineComponent getText(ItemStack stack) {
        return MultiLineComponent.fromTag(stack.getOrCreateTag().getList("text", Tag.TAG_STRING));
    }

    public double getScale(ItemStack stack) {
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
        for (String line : text.split("\n")) listTag.add(StringTag.valueOf(line));
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
