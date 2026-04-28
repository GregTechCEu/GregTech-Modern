package com.lowdragmc.lowdraglib.jei;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import net.minecraft.client.gui.GuiGraphics;

import mezz.jei.api.gui.drawable.IDrawable;

public interface IGui2IDrawable {

    static IDrawable toDrawable(IGuiTexture texture, int width, int height) {
        return new IDrawable() {

            @Override
            public int getWidth() {
                return width;
            }

            @Override
            public int getHeight() {
                return height;
            }

            @Override
            public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
                texture.draw(graphics, 0, 0, xOffset, yOffset, width, height);
            }
        };
    }
}
