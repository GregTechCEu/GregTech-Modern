package brachy.modularui.theme;

import brachy.modularui.api.IThemeApi;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.utils.Color;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class TextFieldTheme extends WidgetTheme {

    @Getter
    private final int markedColor;
    @Getter
    private final int hintColor;

    public TextFieldTheme(int markedColor, int hintColor) {
        this(56, 18, GuiTextures.DISPLAY_SMALL, Color.WHITE.main, Color.WHITE.main,
                false, Color.WHITE.main, markedColor, hintColor);
    }

    public TextFieldTheme(int defaultWidth, int defaultHeight, @Nullable IDrawable background,
                          int color, int textColor, boolean textShadow, int iconColor, int markedColor, int hintColor) {
        super(defaultWidth, defaultHeight, background, color, textColor, textShadow, iconColor);
        this.markedColor = markedColor;
        this.hintColor = hintColor;
    }

    @Override
    public WidgetTheme withNoHoverBackground() {
        return new TextFieldTheme(getDefaultWidth(), getDefaultHeight(), IDrawable.NONE, getColor(), getTextColor(),
                isTextShadow(), getIconColor(), this.markedColor, this.hintColor);
    }

    public static class Builder<T extends TextFieldTheme, B extends TextFieldTheme.Builder<T, B>>
            extends WidgetThemeBuilder<T, B> {

        public B markedColor(int markedColor) {
            add(IThemeApi.MARKED_COLOR, ThemeBuilder.colorJson(markedColor));
            return getThis();
        }

        public B hintColor(int hintColor) {
            add(IThemeApi.HINT_COLOR, ThemeBuilder.colorJson(hintColor));
            return getThis();
        }
    }
}
