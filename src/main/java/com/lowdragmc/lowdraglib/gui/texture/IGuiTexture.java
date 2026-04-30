package com.lowdragmc.lowdraglib.gui.texture;

import com.gregtechceu.gtceu.core.compat.GuiGraphics;

import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;

public interface IGuiTexture extends com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture {

    IGuiTexture EMPTY = new EmptyTexture();
    IGuiTexture MISSING_TEXTURE = new ResourceTexture("minecraft:textures/misc/unknown_server.png");

    default void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        // Two render paths exist for ldlib2 textures:
        // - GuiTexture (sub-interface): has its own draw(GUIContext...) method.
        // - Every other IGuiTexture impl (TextTexture, ItemStackTexture, etc.) needs
        // GuiTextureRendererRegistry. The legacy widgets in this bundle dispatch
        // here, so we must route through the registry too — otherwise text/item
        // textures silently render as no-ops.
        var context = GUIContext.of(graphics, mouseX, mouseY, 0);
        if (this instanceof com.lowdragmc.lowdraglib2.gui.texture.GuiTexture texture) {
            texture.draw(context, x, y, width, height);
            return;
        }
        @SuppressWarnings("unchecked")
        var renderer = (com.lowdragmc.lowdraglib2.gui.texture.rendering.GuiTextureRenderer<com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture>) com.lowdragmc.lowdraglib2.gui.texture.rendering.GuiTextureRendererRegistry
                .findRenderer((com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture) this);
        renderer.draw((com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture) this,
                context, x, y, width, height);
    }

    default void drawSubArea(GuiGraphics graphics, float x, float y, float width, float height,
                             float drawnU, float drawnV, float drawnWidth, float drawnHeight) {
        draw(graphics, 0, 0, x, y, (int) width, (int) height);
    }

    default void updateTick() {}

    @Override
    default IGuiTexture setColor(int color) {
        com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture.super.setColor(color);
        return this;
    }

    @Override
    default IGuiTexture rotate(float degree) {
        com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture.super.rotate(degree);
        return this;
    }

    @Override
    default IGuiTexture scale(float scale) {
        com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture.super.scale(scale);
        return this;
    }

    @Override
    default IGuiTexture transform(int xOffset, int yOffset) {
        com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture.super.transform(xOffset, yOffset);
        return this;
    }

    @Override
    default IGuiTexture copy() {
        return this;
    }

    class EmptyTexture implements IGuiTexture {}
}
