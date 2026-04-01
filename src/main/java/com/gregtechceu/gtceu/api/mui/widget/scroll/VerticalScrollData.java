package com.gregtechceu.gtceu.api.mui.widget.scroll;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.GuiDraw;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import com.gregtechceu.gtceu.utils.GTMath;

public class VerticalScrollData extends ScrollData {

    /**
     * Creates vertical scroll data which handles scrolling and scroll bar.
     * Scrollbar is 4 pixels wide and is placed on the right.
     */
    public VerticalScrollData() {
        this(false, DEFAULT_THICKNESS);
    }

    /**
     * Creates vertical scroll data which handles scrolling and scroll bar.
     * Scrollbar is 4 pixels wide.
     *
     * @param leftAlignment if the scroll bar should be placed on the left
     */
    public VerticalScrollData(boolean leftAlignment) {
        this(leftAlignment, DEFAULT_THICKNESS);
    }

    /**
     * Creates vertical scroll data which handles scrolling and scroll bar.
     *
     * @param leftAlignment if the scroll bar should be placed on the left
     * @param thickness     width of the scroll bar in pixel
     */
    public VerticalScrollData(boolean leftAlignment, int thickness) {
        super(GuiAxis.Y, leftAlignment, thickness);
    }

    public VerticalScrollData cancelScrollEdge(boolean cancelScrollEdge) {
        setCancelScrollEdge(cancelScrollEdge);
        return this;
    }

    @Override
    protected int getFallbackThickness(WidgetTheme widgetTheme) {
        return widgetTheme.getDefaultWidth();
    }

    @Override
    public HorizontalScrollData getOtherScrollData(ScrollArea area) {
        return area.getScrollX();
    }

    @Override
    public boolean isInsideScrollbarArea(ScrollArea area, int x, int y) {
        if (!isScrollBarActive(area)) {
            return false;
        }
        int scrollbar = getThickness();
        ScrollData data = getOtherScrollData(area);
        if (data != null && isOtherScrollBarActive(area, true)) {
            int thickness = data.getThickness();
            if (data.isAxisStart() ? y < thickness : y >= area.h() - thickness) {
                return false;
            }
        }
        return isAxisStart() ? x >= 0 && x < scrollbar : x >= area.w() - scrollbar && x < area.w();
    }

    @Override
    public void drawScrollbar(ModularGuiContext context, ScrollArea area, WidgetTheme widgetTheme, IDrawable texture) {
        boolean isOtherActive = isOtherScrollBarActive(area, true);
        int l = this.getScrollBarLength(area);
        int x = isAxisStart() ? 0 : area.w() - getThickness();
        int y = 0;
        int w = getThickness();
        int h = area.height;
        GuiDraw.drawRect(context.getGraphics(), x, y, w, h, area.getScrollBarBackgroundColor());

        y = getScrollBarStart(area, l, isOtherActive);
        ScrollData data2 = getOtherScrollData(area);
        if (data2 != null && isOtherActive && data2.isAxisStart()) {
            y += data2.getThickness();
        }
        h = l;
        drawScrollBar(context, x, y, w, h, widgetTheme, texture);
    }

    @Override
    public void drawScrollShadow(ScrollArea area, ModularGuiContext context) {
        int min = 0, max = getScrollSize() - getFullVisibleSize(area);
        int s = getScroll();
        final float maxOpacityScroll = 30f;
        final int maxShadowSize = 10;

        int endColor = 0;
        int startColorFull = Color.BLACK.brighter(4);

        ScrollPadding sp = area.getScrollPadding();
        int x = sp.getScrollPaddingLeft();
        int w = area.width - sp.horizontalScrollPadding();
        final int maxShadowSizeLimit = (area.h() - sp.verticalScrollPadding()) / 3;

        if (s > min) {
            float prog = GTMath.clamp(s / maxOpacityScroll, 0, 1);
            int startColor = Color.withAlpha(startColorFull, prog * 0.8f);
            int size = Math.min((int) (prog * maxShadowSize), maxShadowSizeLimit);
            GuiDraw.drawVerticalGradientRect(context.getGraphics(), x, sp.getScrollPaddingTop(), w, size, startColor,
                    endColor);
        }
        if (s < max) {
            float prog = GTMath.clamp((max - s) / maxOpacityScroll, 0, 1);
            int startColor = Color.withAlpha(startColorFull, prog * 0.8f);
            int size = Math.min((int) (prog * maxShadowSize), maxShadowSizeLimit);
            GuiDraw.drawVerticalGradientRect(context.getGraphics(), x, area.h() - size - sp.getScrollPaddingBottom(), w,
                    size, endColor, startColor);
        }
    }
}
