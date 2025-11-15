package com.gregtechceu.gtceu.api.mui.widget.scroll;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.GuiDraw;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

public class HorizontalScrollData extends ScrollData {

    /**
     * Creates horizontal scroll data which handles scrolling and scroll bar.
     * Scrollbar is 4 pixels high and is placed at the bottom.
     */
    public HorizontalScrollData() {
        this(false, DEFAULT_THICKNESS);
    }

    /**
     * Creates horizontal scroll data which handles scrolling and scroll bar.
     * Scrollbar is 4 pixels high.
     *
     * @param topAlignment if the scroll bar should be placed at the top
     */
    public HorizontalScrollData(boolean topAlignment) {
        this(topAlignment, DEFAULT_THICKNESS);
    }

    /**
     * Creates horizontal scroll data which handles scrolling and scroll bar.
     *
     * @param topAlignment if the scroll bar should be placed at the top
     * @param thickness    height of the scroll bar in pixels
     */
    public HorizontalScrollData(boolean topAlignment, int thickness) {
        super(GuiAxis.X, topAlignment, thickness);
    }

    public HorizontalScrollData cancelScrollEdge(boolean cancelScrollEdge) {
        setCancelScrollEdge(cancelScrollEdge);
        return this;
    }

    @Override
    protected int getFallbackThickness(WidgetTheme widgetTheme) {
        return widgetTheme.getDefaultHeight();
    }

    @Override
    public VerticalScrollData getOtherScrollData(ScrollArea area) {
        return area.getScrollY();
    }

    @Override
    public boolean isInsideScrollbarArea(ScrollArea area, int x, int y) {
        if (!isScrollBarActive(area, false)) {
            return false;
        }
        int scrollbar = getThickness();
        ScrollData data = getOtherScrollData(area);
        if (data != null && isOtherScrollBarActive(area, true)) {
            int thickness = data.getThickness();
            if (data.isAxisStart() ? x < thickness : x >= area.w() - thickness) {
                return false;
            }
        }
        return isAxisStart() ? y >= 0 && y < scrollbar : y >= area.h() - scrollbar && y < area.h();
    }

    @Override
    public void drawScrollbar(GuiContext context, ScrollArea area, WidgetTheme widgetTheme, IDrawable texture) {
        boolean isOtherActive = isOtherScrollBarActive(area, true);
        int l = getScrollBarLength(area);
        int x = 0;
        int y = isAxisStart() ? 0 : area.height - getThickness();
        int w = area.width;
        int h = getThickness();
        GuiDraw.drawRect(context.getGraphics(), x, y, w, h, area.getScrollBarBackgroundColor());

        x = getScrollBarStart(area, l, isOtherActive);
        ScrollData data2 = getOtherScrollData(area);
        if (data2 != null && isOtherActive && data2.isAxisStart()) {
            x += data2.getThickness();
        }

        w = l;
        drawScrollBar(context, x, y, w, h, widgetTheme, texture);
    }
}
