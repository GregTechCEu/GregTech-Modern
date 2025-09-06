package com.gregtechceu.gtceu.api.mui.theme;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.mui.base.IThemeApi;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.utils.serialization.json.JsonHelper;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class WidgetThemeSelectable extends WidgetTheme {

    @Getter
    private final WidgetTheme selected;

    public WidgetThemeSelectable(@Nullable IDrawable background, @Nullable IDrawable hoverBackground,
                                 int color, int textColor, boolean textShadow,
                                 @Nullable IDrawable selectedBackground, @Nullable IDrawable selectedHoverBackground,
                                 int selectedColor, int selectedTextColor, boolean selectedTextShadow) {
        super(background, hoverBackground, color, textColor, textShadow);
        this.selected = new WidgetTheme(selectedBackground, selectedHoverBackground, selectedColor, selectedTextColor,
                selectedTextShadow);
    }

    public WidgetThemeSelectable(WidgetTheme parent, JsonObject json, JsonObject fallback) {
        super(parent, json, fallback);
        WidgetThemeSelectable parentWTBT = (WidgetThemeSelectable) parent;
        IDrawable selectedBackground = JsonHelper.deserializeWithFallback(json, fallback, IDrawable.class,
                parentWTBT.getSelected().getBackground(), IThemeApi.SELECTED_BACKGROUND);
        IDrawable selectedHoverBackground = JsonHelper.deserializeWithFallback(json, fallback, IDrawable.class,
                parentWTBT.getSelected().getHoverBackground(), IThemeApi.SELECTED_HOVER_BACKGROUND);
        int selectedColor = JsonHelper.getColorWithFallback(json, fallback, parentWTBT.getSelected().getColor(),
                IThemeApi.SELECTED_COLOR);
        int selectedTextColor = JsonHelper.getColorWithFallback(json, fallback, parentWTBT.getSelected().getTextColor(),
                IThemeApi.SELECTED_TEXT_COLOR);
        boolean selectedTextShadow = JsonHelper.getBoolWithFallback(json, fallback,
                parentWTBT.getSelected().getTextShadow(), IThemeApi.SELECTED_TEXT_SHADOW);
        this.selected = new WidgetTheme(selectedBackground, selectedHoverBackground, selectedColor, selectedTextColor,
                selectedTextShadow);
    }
}
