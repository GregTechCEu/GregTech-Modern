package com.gregtechceu.gtceu.api.mui.theme;

import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;
import com.gregtechceu.gtceu.config.ConfigHolder;

public abstract class AbstractDefaultTheme implements ITheme {

    private WidgetTheme panel;
    private WidgetTheme button;
    private WidgetSlotTheme itemSlot;
    private WidgetSlotTheme fluidSlot;
    private WidgetTextFieldTheme textField;
    private WidgetThemeSelectable toggleButtonTheme;

    @Override
    public ITheme getParentTheme() {
        return null;
    }

    @Override
    public WidgetTheme getPanelTheme() {
        if (this.panel == null) {
            this.panel = getWidgetTheme(Theme.PANEL);
        }
        return this.panel;
    }

    @Override
    public WidgetTheme getButtonTheme() {
        if (this.button == null) {
            this.button = getWidgetTheme(Theme.BUTTON);
        }
        return this.button;
    }

    @Override
    public WidgetSlotTheme getItemSlotTheme() {
        if (this.itemSlot == null) {
            this.itemSlot = (WidgetSlotTheme) getWidgetTheme(Theme.ITEM_SLOT);
        }
        return this.itemSlot;
    }

    @Override
    public WidgetSlotTheme getFluidSlotTheme() {
        if (this.fluidSlot == null) {
            this.fluidSlot = (WidgetSlotTheme) getWidgetTheme(Theme.FLUID_SLOT);
        }
        return this.fluidSlot;
    }

    @Override
    public WidgetTextFieldTheme getTextFieldTheme() {
        if (this.textField == null) {
            this.textField = (WidgetTextFieldTheme) getWidgetTheme(Theme.TEXT_FIELD);
        }
        return this.textField;
    }

    @Override
    public WidgetThemeSelectable getToggleButtonTheme() {
        if (this.toggleButtonTheme == null) {
            this.toggleButtonTheme = (WidgetThemeSelectable) getWidgetTheme(Theme.TOGGLE_BUTTON);
        }
        return this.toggleButtonTheme;
    }

    @Override
    public int getOpenCloseAnimationOverride() {
        // convert 1/60s to ms
        return (int) (ConfigHolder.INSTANCE.client.ui.animationTime * 16.66f);
    }

    @Override
    public boolean getSmoothProgressBarOverride() {
        return ConfigHolder.INSTANCE.client.ui.smoothProgressBar;
    }

    @Override
    public RichTooltip.Pos getTooltipPosOverride() {
        return ConfigHolder.INSTANCE.client.ui.tooltipPos;
    }
}
