package com.gregtechceu.gtceu.common.item.modules;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.client.renderer.monitor.IMonitorRenderer;
import com.gregtechceu.gtceu.client.renderer.monitor.MonitorTextRenderer;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.SCPacketMonitorGroupNBTChange;
import com.gregtechceu.gtceu.common.placeholders.MultiLineComponent;
import com.gregtechceu.gtceu.common.placeholders.PlaceholderContext;

import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.codeeditor.CodeEditorWidget;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TextModuleBehaviour implements IMonitorModuleItem {

    private void updateText(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        StringBuilder formatStringLines = new StringBuilder();
        ListTag tag = stack.getOrCreateTag().getList("formatStringLines", StringTag.TAG_STRING);
        for (Tag value : tag) {
            formatStringLines.append(value.getAsString()).append('\n');
        }
        MultiLineComponent text = GTCEu.PLACEHOLDER_HANDLER.processPlaceholders(
                formatStringLines.toString(),
                new PlaceholderContext(
                        machine.getLevel(),
                        group.getTarget(machine.getLevel()),
                        group.getTargetCoverSide(),
                        group.getItemStackHandler(),
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
                MultiLineComponent.fromTag(stack.getOrCreateTag().getList("text", Tag.TAG_STRING)).toImmutable());
    }

    @Override
    public Widget createUIWidget(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        WidgetGroup builder = new WidgetGroup();
        CodeEditorWidget editor = new CodeEditorWidget(0, 0, 120, 40);
        ButtonWidget saveButton = new ButtonWidget(-40, 22, 20, 20, click -> {
            if (!click.isRemote) return;
            ListTag listTag = new ListTag();
            editor.getLines().forEach(line -> listTag.add(StringTag.valueOf(line)));
            CompoundTag tag2 = stack.getOrCreateTag();
            tag2.put("formatStringLines", listTag);
            stack.setTag(tag2);
            GTNetwork.sendToServer(new SCPacketMonitorGroupNBTChange(stack, group, machine));
        });
        saveButton.setButtonTexture(GuiTextures.BUTTON_CHECK);
        ListTag tag = stack.getOrCreateTag().getList("formatStringLines", Tag.TAG_STRING);
        List<String> formatStringLines = new ArrayList<>();
        for (Tag line : tag) formatStringLines.add(line.getAsString());
        editor.setLines(formatStringLines);
        builder.addWidget(editor);
        builder.addWidget(saveButton);
        return builder;
    }
}
