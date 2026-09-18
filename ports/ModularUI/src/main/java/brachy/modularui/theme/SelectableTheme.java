package brachy.modularui.theme;

import brachy.modularui.api.IThemeApi;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.serialization.json.JsonBuilder;

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
        this(defaultWidth, defaultHeight, background, color, textColor, textShadow, iconColor,
                new WidgetTheme(defaultWidth, defaultHeight, selectedBackground, selectedColor,
                        selectedTextColor, selectedTextShadow, selectedIconColor));
    }

    public SelectableTheme(int defaultWidth, int defaultHeight, @Nullable IDrawable background,
                           int color, int textColor, boolean textShadow, int iconColor,
                           WidgetTheme selected) {
        super(defaultWidth, defaultHeight, background, color, textColor, textShadow, iconColor);
        this.selected = selected;
    }

    @Override
    public WidgetTheme withNoHoverBackground() {
        return new SelectableTheme(getDefaultWidth(), getDefaultHeight(), IDrawable.NONE, getColor(), getTextColor(),
                isTextShadow(), getIconColor(), IDrawable.NONE, this.selected.getColor(), this.selected.getTextColor(),
                this.selected.isTextShadow(), this.selected.getIconColor());
    }

    public @Nullable IDrawable getSelectedBackground() {
        return this.selected.getBackground();
    }

    public int getSelectedColor() {
        return this.selected.getColor();
    }

    public int getSelectedTextColor() {
        return this.selected.getTextColor();
    }

    public boolean isSelectedTextShadow() {
        return this.selected.isTextShadow();
    }

    public int getSelectedIconColor() {
        return this.selected.getIconColor();
    }

    public static class Builder<T extends SelectableTheme, B extends SelectableTheme.Builder<T, B>>
            extends WidgetThemeBuilder<T, B> {

        public B selectedColor(int color) {
            add(IThemeApi.SELECTED_COLOR, ThemeBuilder.colorJson(color));
            return getThis();
        }

        public B selectedTextColor(int color) {
            add(IThemeApi.SELECTED_TEXT_COLOR, ThemeBuilder.colorJson(color));
            return getThis();
        }

        public B selectedTextShadow(int shadow) {
            add(IThemeApi.SELECTED_TEXT_SHADOW, shadow);
            return getThis();
        }

        public B selectedIconColor(int color) {
            add(IThemeApi.SELECTED_ICON_COLOR, ThemeBuilder.colorJson(color));
            return getThis();
        }

        public B selectedBackground(JsonBuilder builder) {
            if (builder instanceof WidgetThemeBuilder<?, ?>) {
                throw new IllegalArgumentException(".selectedBackground() does not accept widget theme builders");
            }
            add(IThemeApi.SELECTED_BACKGROUND, builder);
            return getThis();
        }

        public B selectedBackground(IDrawable drawable) {
            add(IThemeApi.SELECTED_BACKGROUND, IDrawable.toJsonOrThrow(drawable));
            return getThis();
        }

        public B selectedBackground(String textureId) {
            return selectedBackground(ThemeBuilder.textureJson(textureId));
        }
    }
}
