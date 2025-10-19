package com.gregtechceu.gtceu.api.mui.theme;

import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.IThemeApi;

import lombok.Getter;

public abstract class AbstractTheme implements ITheme {

    @Getter
    private final String id;
    @Getter
    private final ITheme parentTheme;

    private WidgetThemeEntry<WidgetTheme> fallback;
    private WidgetThemeEntry<WidgetTheme> panel;
    private WidgetThemeEntry<WidgetTheme> button;
    private WidgetThemeEntry<WidgetTheme> scrollbar;
    private WidgetThemeEntry<SlotTheme> itemSlot;
    private WidgetThemeEntry<SlotTheme> fluidSlot;
    private WidgetThemeEntry<TextFieldTheme> textField;
    private WidgetThemeEntry<SelectableTheme> toggleButtonTheme;

    protected AbstractTheme(String id, ITheme parentTheme) {
        this.id = id;
        this.parentTheme = parentTheme;
    }

    @Override
    public WidgetThemeEntry<WidgetTheme> getFallback() {
        if (fallback == null) {
            fallback = getWidgetTheme(IThemeApi.FALLBACK);
        }
        return fallback;
    }

    @Override
    public WidgetThemeEntry<WidgetTheme> getPanelTheme() {
        if (panel == null) {
            panel = getWidgetTheme(IThemeApi.PANEL);
        }
        return panel;
    }

    @Override
    public WidgetThemeEntry<WidgetTheme> getButtonTheme() {
        if (button == null) {
            button = getWidgetTheme(IThemeApi.BUTTON);
        }
        return button;
    }

    @Override
    public WidgetThemeEntry<WidgetTheme> getScrollbarTheme() {
        if (scrollbar == null) {
            scrollbar = getWidgetTheme(IThemeApi.SCROLLBAR);
        }
        return scrollbar;
    }

    @Override
    public WidgetThemeEntry<SlotTheme> getItemSlotTheme() {
        if (itemSlot == null) {
            itemSlot = getWidgetTheme(IThemeApi.ITEM_SLOT);
        }
        return itemSlot;
    }

    @Override
    public WidgetThemeEntry<SlotTheme> getFluidSlotTheme() {
        if (fluidSlot == null) {
            fluidSlot = getWidgetTheme(IThemeApi.FLUID_SLOT);
        }
        return fluidSlot;
    }

    @Override
    public WidgetThemeEntry<TextFieldTheme> getTextFieldTheme() {
        if (textField == null) {
            textField = getWidgetTheme(IThemeApi.TEXT_FIELD);
        }
        return textField;
    }

    @Override
    public WidgetThemeEntry<SelectableTheme> getToggleButtonTheme() {
        if (toggleButtonTheme == null) {
            toggleButtonTheme = getWidgetTheme(IThemeApi.TOGGLE_BUTTON);
        }
        return toggleButtonTheme;
    }
}
