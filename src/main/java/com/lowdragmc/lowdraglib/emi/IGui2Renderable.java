package com.lowdragmc.lowdraglib.emi;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import dev.emi.emi.api.render.EmiRenderable;

public interface IGui2Renderable {

    static EmiRenderable toDrawable(IGuiTexture texture, int width, int height) {
        return (graphics, x, y, delta) -> texture.draw(
                (com.gregtechceu.gtceu.core.compat.GuiGraphics) (Object) graphics,
                0, 0, x, y, width, height);
    }
}
