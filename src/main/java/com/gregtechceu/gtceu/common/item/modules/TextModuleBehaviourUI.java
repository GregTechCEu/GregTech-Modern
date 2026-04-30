package com.gregtechceu.gtceu.common.item.modules;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.placeholder.PlaceholderHandlerUI;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.item.datacomponents.TextLineList;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.common.network.packets.SCPacketMonitorGroupNBTChange;

import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.codeeditor.CodeEditorWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

final class TextModuleBehaviourUI {

    private TextModuleBehaviourUI() {}

    static Object create(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        WidgetGroup builder = new WidgetGroup();
        CodeEditorWidget editor = new CodeEditorWidget(0, 0, 120, 80);
        TextFieldWidget scaleInput = new TextFieldWidget(
                -50, 47,
                40, 10,
                null,
                null);
        ButtonWidget saveButton = new ButtonWidget(-40, 22, 20, 20, click -> {
            if (!click.isRemote) return;
            List<Component> lines = editor.getLines().stream()
                    .map(Component::literal)
                    .collect(Collectors.toList());
            float scale = 1.0f;
            try {
                scale = Float.parseFloat(scaleInput.getCurrentString());
            } catch (NumberFormatException ignored) {}
            stack.set(GTDataComponents.FORMAT_STRING_LIST, new TextLineList(lines, scale));
            ClientPacketDistributor.sendToServer(new SCPacketMonitorGroupNBTChange(stack, group, machine));
        });
        saveButton.setButtonTexture(GuiTextures.BUTTON_CHECK);
        List<Boolean> tmp = new ArrayList<>();
        Supplier<String> scaleInputSupplier = () -> {
            if (tmp.isEmpty()) {
                tmp.add(true);
            } else {
                scaleInput.setTextSupplier(null);
            }
            if (!stack.has(GTDataComponents.FORMAT_STRING_LIST)) {
                stack.update(GTDataComponents.FORMAT_STRING_LIST, TextLineList.EMPTY,
                        lines -> lines.withScale(1.0f));
                ClientPacketDistributor.sendToServer(new SCPacketMonitorGroupNBTChange(stack, group, machine));
                return "1";
            }
            // noinspection DataFlowIssue
            return String.valueOf(Mth.clamp(stack.get(GTDataComponents.FORMAT_STRING_LIST).scale(), .0001f, 1000f));
        };
        scaleInput.setTextSupplier(scaleInputSupplier);
        scaleInput.setHoverTooltips(Component.translatable("gtceu.gui.central_monitor.text_scale"));
        List<String> formatStringLines = stack.getOrDefault(GTDataComponents.FORMAT_STRING_LIST, TextLineList.EMPTY)
                .lines()
                .stream()
                .map(Component::getString)
                .toList();
        editor.setLines(formatStringLines);
        builder.addWidget(editor);
        builder.addWidget(saveButton);
        Widget placeholderReference = PlaceholderHandlerUI.getPlaceholderHandlerUI("");
        builder.addWidget(scaleInput);
        placeholderReference.setSelfPosition(-100, -50);
        builder.addWidget(placeholderReference);
        return builder;
    }
}
