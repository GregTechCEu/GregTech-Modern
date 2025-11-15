package com.gregtechceu.gtceu.api.mui.theme;

import com.gregtechceu.gtceu.api.mui.base.IThemeApi;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.DrawableSerialization;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.utils.serialization.json.JsonBuilder;
import com.gregtechceu.gtceu.utils.serialization.json.JsonHelper;

import com.google.gson.JsonObject;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class SelectableTheme extends WidgetTheme {

    @Getter
    private final WidgetTheme selected;

    public static SelectableTheme darkTextNoShadow(int defaultWidth, int defaultHeight, @Nullable IDrawable background,
                                                   @Nullable IDrawable selectedBackground) {
        return new SelectableTheme(defaultWidth, defaultHeight,
                background, Color.WHITE.main, Color.TEXT_COLOR_DARK, false, Color.WHITE.main,
                selectedBackground, Color.WHITE.main, Color.TEXT_COLOR_DARK, false, Color.WHITE.main);
    }

    public static SelectableTheme whiteTextShadow(int defaultWidth, int defaultHeight, @Nullable IDrawable background,
                                                  @Nullable IDrawable selectedBackground) {
        return new SelectableTheme(defaultWidth, defaultHeight,
                background, Color.WHITE.main, Color.WHITE.main, true, Color.WHITE.main,
                selectedBackground, Color.WHITE.main, Color.WHITE.main, true, Color.WHITE.main);
    }

    public SelectableTheme(int defaultWidth, int defaultHeight, @Nullable IDrawable background,
                           int color, int textColor, boolean textShadow, int iconColor,
                           @Nullable IDrawable selectedBackground, int selectedColor,
                           int selectedTextColor, boolean selectedTextShadow, int selectedIconColor) {
        super(defaultWidth, defaultHeight, background, color, textColor, textShadow, iconColor);
        this.selected = new WidgetTheme(defaultWidth, defaultHeight, selectedBackground, selectedColor,
                selectedTextColor, selectedTextShadow, selectedIconColor);
    }

    public SelectableTheme(SelectableTheme parent, JsonObject json, JsonObject fallback) {
        super(parent, json, fallback);
        IDrawable selectedBackground = JsonHelper.deserializeWithFallback(json, fallback, IDrawable.class,
                parent.getSelected().getBackground(), IThemeApi.SELECTED_BACKGROUND);
        int selectedColor = JsonHelper.getColorWithFallback(json, fallback,
                parent.getSelected().getColor(), IThemeApi.SELECTED_COLOR);
        int selectedTextColor = JsonHelper.getColorWithFallback(json, fallback,
                parent.getSelected().getTextColor(), IThemeApi.SELECTED_TEXT_COLOR);
        boolean selectedTextShadow = JsonHelper.getBoolWithFallback(json, fallback,
                parent.getSelected().isTextShadow(), IThemeApi.SELECTED_TEXT_SHADOW);
        int selectedIconColor = JsonHelper.getColorWithFallback(json, fallback,
                parent.getSelected().getIconColor(), IThemeApi.SELECTED_ICON_COLOR);
        this.selected = new WidgetTheme(getDefaultWidth(), getDefaultHeight(), selectedBackground, selectedColor,
                selectedTextColor, selectedTextShadow, selectedIconColor);
    }

    @Override
    public WidgetTheme withNoHoverBackground() {
        return new SelectableTheme(getDefaultWidth(), getDefaultHeight(), IDrawable.NONE, getColor(), getTextColor(),
                isTextShadow(), getIconColor(), IDrawable.NONE, this.selected.getColor(), this.selected.getTextColor(),
                this.selected.isTextShadow(), this.selected.getIconColor());
    }

    public static class Builder<T extends SelectableTheme, B extends SelectableTheme.Builder<T, B>>
                               extends WidgetThemeBuilder<T, B> {

        public B selectedColor(int color) {
            add(IThemeApi.SELECTED_COLOR, color);
            return getThis();
        }

        public B selectedTextColor(int color) {
            add(IThemeApi.SELECTED_TEXT_COLOR, color);
            return getThis();
        }

        public B selectedTextShadow(int shadow) {
            add(IThemeApi.SELECTED_TEXT_SHADOW, shadow);
            return getThis();
        }

        public B selectedIconColor(int color) {
            add(IThemeApi.SELECTED_ICON_COLOR, color);
            return getThis();
        }

        public B selectedBackground(JsonBuilder builder) {
            add(IThemeApi.SELECTED_BACKGROUND, builder);
            return getThis();
        }

        public B selectedBackground(IDrawable drawable) {
            add(IThemeApi.SELECTED_BACKGROUND, DrawableSerialization.serialize(drawable));
            return getThis();
        }

        public B selectedBackground(String textureId) {
            return background(new JsonBuilder().add("type", "texture").add("id", textureId));
        }
    }
}
