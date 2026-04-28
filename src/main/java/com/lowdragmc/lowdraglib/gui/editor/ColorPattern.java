package com.lowdragmc.lowdraglib.gui.editor;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;

public enum ColorPattern {

    WHITE(0xFFFFFFFF),
    GRAY(0xFF808080),
    BLACK(0xFF000000),
    RED(0xFFFF3333),
    CYAN(0xFF00FFFF),
    T_WHITE(0x80FFFFFF),
    T_GRAY(0x80808080),
    T_GREEN(0x8033CC33),
    T_RED(0x80CC3333);

    public final int color;

    ColorPattern(int color) {
        this.color = color;
    }

    public ColorRectTexture rectTexture() {
        return new ColorRectTexture(color);
    }

    public ColorRectTexture borderTexture(int border) {
        return new ColorRectTexture(color);
    }
}
