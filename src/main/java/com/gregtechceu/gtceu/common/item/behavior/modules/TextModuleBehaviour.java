package com.gregtechceu.gtceu.common.item.behavior.modules;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
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
import com.gregtechceu.gtceu.common.network.packets.SCPacketMonitorGroupNBTChange;
import com.gregtechceu.gtceu.data.item.GTDataComponents;

import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.codeeditor.CodeEditorWidget;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.network.PacketDistributor;

import org.jetbrains.annotations.Nullable;

import org.apache.commons.lang3.mutable.MutableFloat;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
    public IMonitorRenderer getRenderer(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        TextLineList lines = stack.getOrDefault(GTDataComponents.TEXT_LINE_LIST, TextLineList.EMPTY);
        return new MonitorTextRenderer(lines.lines(), Math.max(lines.scale(), 0.0001f));
    }

    @Override
    public Widget createUIWidget(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        WidgetGroup builder = new WidgetGroup();
        CodeEditorWidget editor = new CodeEditorWidget(0, 0, 120, 80);
        // editor.codeEditor.setLanguageDefinition(PlaceholderHandler.LANG_DEFINITION);

        float scaleValue = stack.getOrDefault(GTDataComponents.TEXT_LINE_LIST, TextLineList.EMPTY).scale();
        if (!stack.has(GTDataComponents.TEXT_LINE_LIST)) {
            stack.set(GTDataComponents.TEXT_LINE_LIST, TextLineList.EMPTY);
            PacketDistributor.sendToServer(new SCPacketMonitorGroupNBTChange(stack, group, machine));
        }
        MutableFloat currentScale = new MutableFloat(scaleValue);
        FloatInputWidget scaleInput = new FloatInputWidget(
                -50, 47,
                40, 10,
                currentScale::getValue,
                currentScale::setValue);
        scaleInput.setHoverTooltips(Component.translatable("gtceu.gui.central_monitor.text_scale"));
        builder.addWidget(scaleInput);

        ButtonWidget saveButton = new ButtonWidget(-40, 22, 20, 20, click -> {
            if (!click.isRemote) return;
            stack.set(GTDataComponents.FORMAT_STRING_LIST, new FormatStringList(editor.getLines()));

            final float newValue = Mth.clamp(currentScale.floatValue(), 0.0001f, 1000.0f);
            stack.update(GTDataComponents.TEXT_LINE_LIST, TextLineList.EMPTY, lines -> lines.withScale(newValue));
            PacketDistributor.sendToServer(new SCPacketMonitorGroupNBTChange(stack, group, machine));
        });
        saveButton.setButtonTexture(GuiTextures.BUTTON_CHECK);
        builder.addWidget(saveButton);

        List<String> formatStringLines = stack.getOrDefault(GTDataComponents.FORMAT_STRING_LIST, FormatStringList.EMPTY)
                .lines();
        editor.setLines(formatStringLines);
        builder.addWidget(editor);

        Widget placeholderReference = PlaceholderHandler.getPlaceholderHandlerUI("");
        placeholderReference.setSelfPosition(-100, -50);
        builder.addWidget(placeholderReference);

        return builder;
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
        List<Component> lines = new ArrayList<>();
        for (String line : text.split("\n")) {
            lines.add(Component.literal(line));
        }
        stack.update(GTDataComponents.TEXT_LINE_LIST, TextLineList.EMPTY,
                textLineList -> textLineList.withLines(lines));
    }

    public String getPlaceholderText(ItemStack stack) {
        StringBuilder formatStringLines = new StringBuilder();
        List<Component> lines = stack.getOrDefault(GTDataComponents.FORMAT_STRING_LIST, FormatStringList.EMPTY).lines();
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
            tooltipComponents.addAll(stack.getOrDefault(GTDataComponents.TEXT_LINE_LIST, TextLineList.EMPTY).lines());
            tooltipComponents.add(Component.literal("Processed text:").withStyle(ChatFormatting.GOLD));
            tooltipComponents.addAll(getText(stack));
        }
    }
}
