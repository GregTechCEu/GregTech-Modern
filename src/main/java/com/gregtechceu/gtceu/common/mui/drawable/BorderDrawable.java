package com.gregtechceu.gtceu.common.mui.drawable;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.IntSupplier;

@Accessors(fluent = true)
public class BorderDrawable implements IDrawable {

    @Getter
    @Setter
    private IntSupplier color = () -> 0xFFFFFFFF;
    @Getter
    @Setter
    private int borderWidth = 2;

    public BorderDrawable() {}

    public BorderDrawable(int color, int borderWidth) {
        this.color = () -> color;
        this.borderWidth = borderWidth;
    }

    public BorderDrawable(IntSupplier color, int borderWidth) {
        this.color = color;
        this.borderWidth = borderWidth;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        context.getGraphics().fill(x - borderWidth, y - borderWidth, x + borderWidth + width, y, color.getAsInt());
        context.getGraphics().fill(x - borderWidth, y - borderWidth, x, y + borderWidth + height, color.getAsInt());
        context.getGraphics().fill(x + width, y - borderWidth, x + width + borderWidth, y + height + borderWidth,
                color.getAsInt());
        context.getGraphics().fill(x - borderWidth, y + height, x + borderWidth + width, y + height + borderWidth,
                color.getAsInt());
    }
}
