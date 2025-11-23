package com.gregtechceu.gtceu.api.mui.theme;

import com.gregtechceu.gtceu.api.mui.base.IThemeApi;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.serialization.json.JsonHelper;

import com.google.gson.JsonObject;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class TextFieldTheme extends WidgetTheme {

    @Getter
    private final int markedColor;
    @Getter
    private final int hintColor;

    public TextFieldTheme(int markedColor, int hintColor) {
        this(56, 18, GTGuiTextures.MUI_DISPLAY_SMALL, Color.WHITE.main, Color.WHITE.main,
                false, Color.WHITE.main, markedColor, hintColor);
    }

    public TextFieldTheme(int defaultWidth, int defaultHeight, @Nullable IDrawable background,
                          int color, int textColor, boolean textShadow, int iconColor, int markedColor, int hintColor) {
        super(defaultWidth, defaultHeight, background, color, textColor, textShadow, iconColor);
        this.markedColor = markedColor;
        this.hintColor = hintColor;
    }

    public TextFieldTheme(TextFieldTheme parent, JsonObject json, JsonObject fallback) {
        super(parent, json, fallback);
        this.markedColor = JsonHelper.getColorWithFallback(json, fallback, parent.getMarkedColor(),
                IThemeApi.MARKED_COLOR);
        this.hintColor = JsonHelper.getColorWithFallback(json, fallback, parent.getHintColor(), IThemeApi.HINT_COLOR);
    }

    @Override
    public WidgetTheme withNoHoverBackground() {
        return new TextFieldTheme(getDefaultWidth(), getDefaultHeight(), IDrawable.NONE, getColor(), getTextColor(),
                isTextShadow(), getIconColor(), this.markedColor, this.hintColor);
    }

    public static class Builder<T extends TextFieldTheme, B extends TextFieldTheme.Builder<T, B>>
                               extends WidgetThemeBuilder<T, B> {

        public B markedColor(int markedColor) {
            add(IThemeApi.MARKED_COLOR, markedColor);
            return getThis();
        }

        public B hintColor(int hintColor) {
            add(IThemeApi.HINT_COLOR, hintColor);
            return getThis();
        }
    }
}
