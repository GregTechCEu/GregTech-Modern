package com.gregtechceu.gtceu.api.mui.base.widget;

public enum ResizeDragArea {

    TOP_LEFT(true, true, false, false),
    TOP_RIGHT(true, false, false, true),
    BOTTOM_LEFT(false, true, true, false),
    BOTTOM_RIGHT(false, false, true, true),
    TOP(true, false, false, false),
    LEFT(false, true, false, false),
    BOTTOM(false, false, true, false),
    RIGHT(false, false, false, true);

    public final boolean top, left, bottom, right;

    ResizeDragArea(boolean top, boolean left, boolean bottom, boolean right) {
        if (top && bottom || left && right) throw new IllegalArgumentException();
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }
}
