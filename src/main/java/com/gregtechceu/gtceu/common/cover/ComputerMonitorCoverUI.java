package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.placeholder.PlaceholderHandlerUI;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.GTStringUtils;

import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.chat.Component;

final class ComputerMonitorCoverUI {

    private ComputerMonitorCoverUI() {}

    static Widget createUIWidget(ComputerMonitorCover cover) {
        int textFieldWidth = 160, horizontalPadding = 10, verticalPadding = 2;
        final WidgetGroup group = new WidgetGroup(0, 0, 2 * textFieldWidth + 3 * horizontalPadding, 150);
        final WidgetGroup mainPage = new WidgetGroup(0, 0, 2 * textFieldWidth + 3 * horizontalPadding, 150);
        final WidgetGroup formatStringArgsPage = new WidgetGroup(0, 0, 2 * textFieldWidth + 3 * horizontalPadding, 150);
        for (int i = 0; i < 8; i++) {
            TextFieldWidget formatStringInput = new TextFieldWidget();
            formatStringInput.setSize(textFieldWidth, 15);
            formatStringInput.setSelfPosition(horizontalPadding + textFieldWidth / 2,
                    10 + verticalPadding + i * (15 + verticalPadding));
            formatStringInput.setHoverTooltips(GTStringUtils.toImmutable(
                    LangHandler.getMultiLang("gtceu.gui.computer_monitor_cover.main_textbox_tooltip", i + 1)));
            int finalI = i;
            if (i >= cover.getFormatStringLines().size()) cover.getFormatStringLines().add("");
            formatStringInput.setCurrentString(cover.getFormatStringLines().get(i));
            formatStringInput.setTextResponder((s) -> cover.getFormatStringLines().set(finalI, s));
            mainPage.addWidget(formatStringInput);
            SlotWidget slot = new com.gregtechceu.gtceu.api.gui.widget.SlotWidget(
                    cover.itemStackHandler,
                    i,
                    horizontalPadding + 50,
                    20 * i);
            slot.setBackgroundTexture(SlotWidget.ITEM_SLOT_TEXTURE);
            slot.setHoverTooltips(GTStringUtils
                    .toImmutable(LangHandler.getMultiLang("gtceu.gui.computer_monitor_cover.slot_tooltip", i + 1)));
            mainPage.addWidget(slot);
        }
        for (int i = 0; i < 8; i++) {
            TextFieldWidget formatStringArgsInput = new TextFieldWidget();
            formatStringArgsInput.setSize(textFieldWidth, 15);
            formatStringArgsInput.setSelfPosition(textFieldWidth / 2 + horizontalPadding,
                    10 + verticalPadding + i * (15 + verticalPadding));
            formatStringArgsInput.setHoverTooltips(GTStringUtils.toImmutable(
                    LangHandler.getMultiLang("gtceu.gui.computer_monitor_cover.second_page_textbox_tooltip",
                            GTStringUtils.getIntOrderingSuffix(i + 1))));

            int finalI = i;
            if (i >= cover.getFormatStringArgs().size()) cover.getFormatStringArgs().add("");
            formatStringArgsInput.setCurrentString(cover.getFormatStringArgs().get(i));
            formatStringArgsInput.setTextResponder((s) -> cover.getFormatStringArgs().set(finalI, s));
            formatStringArgsPage.addWidget(formatStringArgsInput);
        }
        ButtonWidget switchToFormatStringArgsPageButton = new ButtonWidget(
                horizontalPadding + 50,
                10 * (15 + verticalPadding) + verticalPadding,
                20, 20,
                new ResourceBorderTexture(),
                clickData -> {
                    group.clearAllWidgets();
                    group.addWidget(formatStringArgsPage);
                });
        ButtonWidget switchBack = new ButtonWidget(
                horizontalPadding + 50,
                10 * (15 + verticalPadding) + verticalPadding,
                20, 20,
                new ResourceBorderTexture(),
                clickData -> {
                    group.clearAllWidgets();
                    group.addWidget(mainPage);
                });
        mainPage.addWidget(PlaceholderHandlerUI.getPlaceholderHandlerUI(""));
        IntInputWidget updateIntervalInput = new IntInputWidget(0, 0, 60, 20, cover::getUpdateInterval,
                cover::setUpdateInterval);
        updateIntervalInput.setMin(1);
        updateIntervalInput.setMax(60 * 20);
        updateIntervalInput
                .setHoverTooltips(Component.translatable("gtceu.gui.computer_monitor_cover.update_interval"));
        mainPage.addWidget(updateIntervalInput);
        switchToFormatStringArgsPageButton
                .setHoverTooltips(Component.translatable("gtceu.gui.computer_monitor_cover.edit_blank_placeholders"));
        switchBack.setHoverTooltips(Component.translatable("gtceu.gui.computer_monitor_cover.edit_displayed_text"));
        mainPage.addWidget(switchToFormatStringArgsPageButton);
        formatStringArgsPage.addWidget(switchBack);
        group.addWidget(mainPage);
        return group;
    }
}
