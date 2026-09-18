package brachy.modularui.theme;

import brachy.modularui.api.IThemeApi;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.utils.Color;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class SlotTheme extends WidgetTheme {

    @Getter
    private final int slotHoverColor;

    public SlotTheme(IDrawable background) {
        this(background, Color.withAlpha(Color.WHITE.main, 0x60));
    }

    public SlotTheme(IDrawable background, int slotHoverColor) {
        this(18, 18, background, Color.WHITE.main, 0xFF404040, false, Color.WHITE.main, slotHoverColor);
    }

    public SlotTheme(int defaultWidth, int defaultHeight, @Nullable IDrawable background, int color, int textColor,
                     boolean textShadow, int iconColor, int slotHoverColor) {
        super(defaultWidth, defaultHeight, background, color, textColor, textShadow, iconColor);
        this.slotHoverColor = slotHoverColor;
    }

    @Override
    public WidgetTheme withNoHoverBackground() {
        return new SlotTheme(getDefaultWidth(), getDefaultHeight(), IDrawable.NONE, getColor(), getTextColor(),
                isTextShadow(), getIconColor(), this.slotHoverColor);
    }

    public static class Builder<T extends SlotTheme, B extends Builder<T, B>> extends WidgetThemeBuilder<T, B> {

        public B hoverColor(int hoverColor) {
            add(IThemeApi.SLOT_HOVER_COLOR, ThemeBuilder.colorJson(hoverColor));
            return getThis();
        }
    }
}
