package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;

public class TiledUITexture extends UITexture {

    private final int imageWidth, imageHeight;

    /**
     * Use {@link UITexture#builder()} with {@link Builder#tiled()}
     */
    TiledUITexture(ResourceLocation location, float u0, float v0, float u1, float v1, int imageWidth, int imageHeight,
                   ColorType colorType, boolean nonOpaque) {
        super(location, u0, v0, u1, v1, colorType, nonOpaque);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    @Override
    public void draw(GuiContext context, float x, float y, float width, float height) {
        if (width == this.imageWidth && height == this.imageHeight) {
            super.draw(context, x, y, width, height);
            return;
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        GuiDraw.drawTiledTexture(context.getLastGraphicsPose(), this.location, x, y, width, height,
                this.u0, this.v0, this.u1, this.v1,
                this.imageWidth, this.imageHeight, 0);
    }

    @Override
    public boolean saveToJson(JsonObject json) {
        super.saveToJson(json);
        if (json.entrySet().size() > 1) {
            json.addProperty("tiled", true);
        }
        return true;
    }

    @Override
    protected TiledUITexture copy() {
        return new TiledUITexture(location, u0, v0, u1, v1, imageWidth, imageHeight, colorType, nonOpaque);
    }

    @Override
    public TiledUITexture withColorOverride(int color) {
        return (TiledUITexture) super.withColorOverride(color);
    }
}
