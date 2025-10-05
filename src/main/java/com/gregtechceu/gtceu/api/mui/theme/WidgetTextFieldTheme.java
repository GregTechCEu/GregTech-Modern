package com.gregtechceu.gtceu.api.mui.theme;

import com.gregtechceu.gtceu.api.mui.base.IThemeApi;
import com.gregtechceu.gtceu.api.mui.drawable.GuiTextures;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.utils.serialization.json.JsonHelper;

import com.google.gson.JsonObject;
import lombok.Getter;

public class WidgetTextFieldTheme extends WidgetTheme {

    @Getter
    private final int markedColor;
    @Getter
    private final int hintColor;

    public WidgetTextFieldTheme(int markedColor, int hintColor) {
        super(GuiTextures.DISPLAY_SMALL, null, Color.WHITE.main, Color.WHITE.main, false);
        this.markedColor = markedColor;
        this.hintColor = hintColor;
    }

    public WidgetTextFieldTheme(WidgetTheme parent, JsonObject fallback, JsonObject json) {
        super(parent, json, fallback);
        this.markedColor = JsonHelper.getColorWithFallback(json, fallback,
                ((WidgetTextFieldTheme) parent).getMarkedColor(), IThemeApi.MARKED_COLOR);
        this.hintColor = JsonHelper.getColorWithFallback(json, fallback, ((WidgetTextFieldTheme) parent).getHintColor(),
                IThemeApi.HINT_COLOR);
    }
}
