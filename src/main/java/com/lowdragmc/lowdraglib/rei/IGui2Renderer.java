package com.lowdragmc.lowdraglib.rei;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import me.shedaniel.rei.api.client.gui.Renderer;

public interface IGui2Renderer {

    static Renderer toDrawable(IGuiTexture texture) {
        return (graphics, bounds, mouseX, mouseY, delta) -> texture.draw(
                (com.gregtechceu.gtceu.core.compat.GuiGraphics) (Object) graphics, mouseX, mouseY, bounds.x, bounds.y,
                bounds.width, bounds.height);
    }
}
