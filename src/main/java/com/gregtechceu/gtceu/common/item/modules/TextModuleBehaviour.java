package com.gregtechceu.gtceu.common.item.modules;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.api.placeholder.MultiLineComponent;
import com.gregtechceu.gtceu.api.placeholder.PlaceholderContext;
import com.gregtechceu.gtceu.api.placeholder.PlaceholderHandler;
import com.gregtechceu.gtceu.client.renderer.monitor.IMonitorRenderer;
import com.gregtechceu.gtceu.client.renderer.monitor.MonitorTextRenderer;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.SCPacketMonitorGroupNBTChange;

import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.codeeditor.CodeEditorWidget;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TextModuleBehaviour implements IMonitorModuleItem {

    private void updateText(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        StringBuilder formatStringLines = new StringBuilder();
        ListTag tag = stack.getOrCreateTag().getList("formatStringLines", StringTag.TAG_STRING);
        for (Tag value : tag) {
            formatStringLines.append(value.getAsString()).append('\n');
        }
        MultiLineComponent text = PlaceholderHandler.processPlaceholders(
                formatStringLines.toString(),
                new PlaceholderContext(
                        machine.getLevel(),
                        group.getTarget(machine.getLevel()),
                        group.getTargetCoverSide(),
                        group.getPlaceholderSlotsHandler(),
                        group.getTargetCover(machine.getLevel()),
                        null));
        stack.getOrCreateTag().put("text", text.toTag());
    }

    @Override
    public void tick(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        this.updateText(stack, machine, group);
    }

    @Override
    public IMonitorRenderer getRenderer(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        return new MonitorTextRenderer(
                MultiLineComponent.fromTag(stack.getOrCreateTag().getList("text", Tag.TAG_STRING)).toImmutable(),
                Math.max(stack.getOrCreateTag().getDouble("scale"), .0001));
    }

    @Override
    public Widget createUIWidget(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        WidgetGroup builder = new WidgetGroup();
        CodeEditorWidget editor = new CodeEditorWidget(0, 0, 120, 80);
        TextFieldWidget scaleInput = new TextFieldWidget(
                -50, 47,
                40, 10,
                null,
                null);
        ButtonWidget saveButton = new ButtonWidget(-40, 22, 20, 20, click -> {
            if (!click.isRemote) return;
            ListTag listTag = new ListTag();
            editor.getLines().forEach(line -> listTag.add(StringTag.valueOf(line)));
            CompoundTag tag2 = stack.getOrCreateTag();
            tag2.put("formatStringLines", listTag);
            try {
                tag2.putDouble("scale", Double.parseDouble(scaleInput.getCurrentString()));
            } catch (NumberFormatException ignored) {}
            stack.setTag(tag2);
            GTNetwork.sendToServer(new SCPacketMonitorGroupNBTChange(stack, group, machine));
        });
        saveButton.setButtonTexture(GuiTextures.BUTTON_CHECK);
        List<Boolean> tmp = new ArrayList<>();
        Supplier<String> scaleInputSupplier = () -> {
            if (tmp.isEmpty()) tmp.add(true);
            else scaleInput.setTextSupplier(null);
            if (!stack.getOrCreateTag().contains("scale")) {
                stack.getOrCreateTag().putDouble("scale", 1);
                GTNetwork.sendToServer(new SCPacketMonitorGroupNBTChange(stack, group, machine));
                return "1";
            }
            return String.valueOf(Mth.clamp(stack.getOrCreateTag().getDouble("scale"), .0001, 1000));
        };
        scaleInput.setTextSupplier(scaleInputSupplier);
        scaleInput.setHoverTooltips(Component.translatable("gtceu.gui.central_monitor.text_scale"));
        ListTag tag = stack.getOrCreateTag().getList("formatStringLines", Tag.TAG_STRING);
        List<String> formatStringLines = new ArrayList<>();
        for (Tag line : tag) formatStringLines.add(line.getAsString());
        editor.setLines(formatStringLines);
        builder.addWidget(editor);
        builder.addWidget(saveButton);
        Widget placeholderReference = PlaceholderHandler.getPlaceholderHandlerUI("");
        builder.addWidget(scaleInput);
        placeholderReference.setSelfPosition(-100, -50);
        builder.addWidget(placeholderReference);
        return builder;
    }
}
