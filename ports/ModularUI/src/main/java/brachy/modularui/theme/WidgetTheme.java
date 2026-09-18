package brachy.modularui.theme;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.utils.Color;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class WidgetTheme {

    public static WidgetThemeEntry<WidgetTheme> getDefault() {
        return ThemeAPI.DEFAULT_THEME.getFallback();
    }

    @Getter private final int defaultWidth;
    @Getter private final int defaultHeight;
    @Getter
    @Nullable
    private final IDrawable background;
    @Getter private final int color;
    @Getter private final int textColor;
    @Getter private final boolean textShadow;
    @Getter private final int iconColor;

    public static WidgetTheme whiteTextShadow(int defaultWidth, int defaultHeight, @Nullable IDrawable background) {
        return new WidgetTheme(defaultWidth, defaultHeight, background, Color.WHITE.main,
                Color.WHITE.main, true, Color.WHITE.main);
    }

    public static WidgetTheme darkTextNoShadow(int defaultWidth, int defaultHeight, @Nullable IDrawable background) {
        return new WidgetTheme(defaultWidth, defaultHeight, background, Color.WHITE.main,
                Color.TEXT_COLOR_DARK, false, Color.WHITE.main);
    }

    public WidgetTheme(int defaultWidth, int defaultHeight, @Nullable IDrawable background,
                       int color, int textColor, boolean textShadow, int iconColor) {
        this.defaultWidth = defaultWidth;
        this.defaultHeight = defaultHeight;
        this.background = background;
        this.color = color;
        this.textColor = textColor == 0 ? color : textColor;
        this.textShadow = textShadow;
        this.iconColor = iconColor == 0 ? color : iconColor;
    }

    protected static boolean inherits(JsonObject json, String property) {
        if (!json.has("inherit")) return false;
        JsonElement element = json.get("inherit");
        if (element.isJsonPrimitive()) return element.getAsString().equals(property);
        if (element.isJsonArray()) {
            for (JsonElement e : element.getAsJsonArray()) {
                if (e.isJsonPrimitive() && e.getAsString().equals(property)) return true;
            }
        }
        return false;
    }

    public WidgetTheme withNoHoverBackground() {
        return new WidgetTheme(this.defaultWidth, this.defaultHeight, IDrawable.NONE, this.color, this.textColor,
                this.textShadow, this.iconColor);
    }
}
