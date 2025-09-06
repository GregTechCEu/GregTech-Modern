package com.gregtechceu.gtceu.api.mui.theme;

import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.IThemeApi;
import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;
import com.gregtechceu.gtceu.config.ConfigHolder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public class Theme implements ITheme {

    public static final String FALLBACK = IThemeApi.FALLBACK;
    public static final String PANEL = IThemeApi.PANEL;
    public static final String BUTTON = IThemeApi.BUTTON;
    public static final String ITEM_SLOT = IThemeApi.ITEM_SLOT;
    public static final String FLUID_SLOT = IThemeApi.FLUID_SLOT;
    public static final String TEXT_FIELD = IThemeApi.TEXT_FIELD;
    public static final String TOGGLE_BUTTON = IThemeApi.TOGGLE_BUTTON;

    private final Map<String, WidgetTheme> widgetThemes = new Object2ObjectOpenHashMap<>();

    @Getter
    private final String id;
    @Getter
    private final ITheme parentTheme;
    @Getter
    private final WidgetTheme fallback;
    @Getter
    private final WidgetTheme panelTheme;
    @Getter
    private final WidgetTheme buttonTheme;
    @Getter
    private final WidgetSlotTheme itemSlotTheme;
    @Getter
    private final WidgetSlotTheme fluidSlotTheme;
    @Getter
    private final WidgetTextFieldTheme textFieldTheme;
    @Getter
    private final WidgetThemeSelectable toggleButtonTheme;

    @Setter
    private int openCloseAnimationOverride = -1;
    @Setter
    private @Nullable Boolean smoothProgressBarOverride = null;
    @Setter
    private @Nullable RichTooltip.Pos tooltipPosOverride = null;

    Theme(String id, ITheme parent, Map<String, WidgetTheme> widgetThemes) {
        this.id = id;
        this.parentTheme = parent;
        this.widgetThemes.putAll(widgetThemes);
        if (parent instanceof Theme theme) {
            for (Map.Entry<String, WidgetTheme> entry : theme.widgetThemes.entrySet()) {
                if (!this.widgetThemes.containsKey(entry.getKey())) {
                    this.widgetThemes.put(entry.getKey(), entry.getValue());
                }
            }
        } else if (parent == IThemeApi.get().getDefaultTheme()) {
            if (!this.widgetThemes.containsKey(FALLBACK)) {
                this.widgetThemes.put(FALLBACK, ThemeManager.defaultdefaultWidgetTheme);
            }
            for (Map.Entry<String, WidgetTheme> entry : ThemeAPI.INSTANCE.defaultWidgetThemes.entrySet()) {
                if (!this.widgetThemes.containsKey(entry.getKey())) {
                    this.widgetThemes.put(entry.getKey(), entry.getValue());
                }
            }
        }
        this.panelTheme = this.widgetThemes.get(PANEL);
        this.fallback = this.widgetThemes.get(FALLBACK);
        this.buttonTheme = this.widgetThemes.get(BUTTON);
        this.itemSlotTheme = (WidgetSlotTheme) this.widgetThemes.get(ITEM_SLOT);
        this.fluidSlotTheme = (WidgetSlotTheme) this.widgetThemes.get(FLUID_SLOT);
        this.textFieldTheme = (WidgetTextFieldTheme) this.widgetThemes.get(TEXT_FIELD);
        this.toggleButtonTheme = (WidgetThemeSelectable) this.widgetThemes.get(TOGGLE_BUTTON);
    }

    public WidgetTheme getWidgetTheme(String id) {
        if (this.widgetThemes.containsKey(id)) {
            return this.widgetThemes.get(id);
        }
        return getFallback();
    }

    @Override
    public int getOpenCloseAnimationOverride() {
        if (this.openCloseAnimationOverride != -1) {
            return this.openCloseAnimationOverride;
        }
        return ConfigHolder.INSTANCE.client.ui.animationTime;
    }

    @Override
    public boolean getSmoothProgressBarOverride() {
        return Objects.requireNonNullElse(this.smoothProgressBarOverride,
                ConfigHolder.INSTANCE.client.ui.smoothProgressBar);
    }

    @Override
    public RichTooltip.Pos getTooltipPosOverride() {
        return Objects.requireNonNullElse(this.tooltipPosOverride, ConfigHolder.INSTANCE.client.ui.tooltipPos);
    }
}
