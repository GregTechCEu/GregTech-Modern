package com.gregtechceu.gtceu.api.mui.theme;

import com.gregtechceu.gtceu.api.mui.base.IThemeApi;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.utils.serialization.json.JsonHelper;

import com.google.gson.JsonObject;
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

    public SlotTheme(SlotTheme parent, JsonObject json, JsonObject fallback) {
        super(parent, json, fallback);
        this.slotHoverColor = JsonHelper.getColorWithFallback(json, fallback, parent.getSlotHoverColor(),
                IThemeApi.SLOT_HOVER_COLOR);
    }

    @Override
    public WidgetTheme withNoHoverBackground() {
        return new SlotTheme(getDefaultWidth(), getDefaultHeight(), IDrawable.NONE, getColor(), getTextColor(),
                isTextShadow(), getIconColor(), this.slotHoverColor);
    }

    public static class Builder<T extends SlotTheme, B extends Builder<T, B>> extends WidgetThemeBuilder<T, B> {

        public B hoverColor(int hoverColor) {
            add(IThemeApi.SLOT_HOVER_COLOR, hoverColor);
            return getThis();
        }
    }
}
