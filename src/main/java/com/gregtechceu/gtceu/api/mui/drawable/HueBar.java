package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

public class HueBar implements IDrawable {

    private static final int[] COLORS = {
            Color.ofHSV(60, 1f, 1f, 1f),
            Color.ofHSV(120, 1f, 1f, 1f),
            Color.ofHSV(180, 1f, 1f, 1f),
            Color.ofHSV(240, 1f, 1f, 1f),
            Color.ofHSV(300, 1f, 1f, 1f),
            Color.ofHSV(0, 1f, 1f, 1f)
    };

    private final GuiAxis axis;

    public HueBar(GuiAxis axis) {
        this.axis = axis;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        int size = this.axis.isHorizontal() ? width : height;
        float step = size / 6f;
        int previous = COLORS[5];
        for (int i = 0; i < 6; i++) {
            int current = COLORS[i];
            if (this.axis.isHorizontal()) {
                GuiDraw.drawHorizontalGradientRect(context.getGraphics(), x + step * i, y, step, height, previous,
                        current);
            } else {
                GuiDraw.drawVerticalGradientRect(context.getGraphics(), x, y + step * i, width, step, previous,
                        current);
            }
            previous = current;
        }
    }
}
