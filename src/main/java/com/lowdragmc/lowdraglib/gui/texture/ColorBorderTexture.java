package com.lowdragmc.lowdraglib.gui.texture;

import net.minecraft.client.gui.GuiGraphics;

public class ColorBorderTexture extends ColorRectTexture {

    private final int border;

    public ColorBorderTexture(int border, int color) {
        super(color);
        this.border = border;
    }

    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        int left = (int) x;
        int top = (int) y;
        graphics.fill(left, top, left + width, top + border, color);
        graphics.fill(left, top + height - border, left + width, top + height, color);
        graphics.fill(left, top, left + border, top + height, color);
        graphics.fill(left + width - border, top, left + width, top + height, color);
    }
}
