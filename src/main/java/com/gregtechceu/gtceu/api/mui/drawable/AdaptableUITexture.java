package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;

public class AdaptableUITexture extends UITexture {

    private final int imageWidth, imageHeight, bl, bt, br, bb;
    private final boolean tiled;

    /**
     * Use {@link UITexture#builder()} with {@link Builder#adaptable(int, int)}
     */
    AdaptableUITexture(ResourceLocation location, float u0, float v0, float u1, float v1, boolean background,
                       int imageWidth, int imageHeight, int bl, int bt, int br, int bb, boolean tiled) {
        super(location, u0, v0, u1, v1, background);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.bl = bl;
        this.bt = bt;
        this.br = br;
        this.bb = bb;
        this.tiled = tiled;
    }

    @Override
    public AdaptableUITexture getSubArea(float uStart, float vStart, float uEnd, float vEnd) {
        return new AdaptableUITexture(this.location, lerpU(uStart), lerpV(vStart), lerpU(uEnd), lerpV(vEnd),
                this.canApplyTheme, this.imageWidth, this.imageHeight, this.bl, this.bt, this.br, this.bb, this.tiled);
    }

    @Override
    public void draw(GuiContext context, float x, float y, float width, float height) {
        if (width == this.imageWidth && height == this.imageHeight) {
            super.draw(context, x, y, width, height);
            return;
        }
        if (this.tiled) {
            drawTiled(context, x, y, width, height);
        } else {
            drawStretched(context, x, y, width, height);
        }
    }

    public void drawStretched(GuiContext context, float x, float y, float width, float height) {
        Matrix4f pose = context.getLastGraphicsPose();

        if (this.bl <= 0 && this.bt <= 0 && this.br <= 0 && this.bb <= 0) {
            super.draw(context, x, y, width, height);
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.location);

        float uBl = this.bl * 1f / this.imageWidth, uBr = this.br * 1f / this.imageWidth;
        float vBt = this.bt * 1f / this.imageHeight, vBb = this.bb * 1f / this.imageHeight;
        float x1 = x + width, y1 = y + height;
        float uInnerStart = this.u0 + uBl, vInnerStart = this.v0 + vBt, uInnerEnd = this.u1 - uBr,
                vInnerEnd = this.v1 - vBb;

        if ((this.bl > 0 || this.br > 0) && this.bt <= 0 && this.bb <= 0) {
            // left border
            GuiDraw.drawTexture(pose, x, y, x + this.bl, y1, this.u0, this.v0, uInnerStart, this.v1);
            // right border
            GuiDraw.drawTexture(pose, x1 - this.br, y, x1, y1, uInnerEnd, this.v0, this.u1, this.v1);
            // center
            GuiDraw.drawTexture(pose, x + this.bl, y, x1 - this.br, y1, uInnerStart, this.v0, uInnerEnd, this.v1);
        } else if (this.bl <= 0 && this.br <= 0) {
            // top border
            GuiDraw.drawTexture(pose, x, y, x1, y + this.bt, this.u0, this.v0, this.u1, vInnerStart);
            // bottom border
            GuiDraw.drawTexture(pose, x, y1 - this.bb, x1, y1, this.u0, vInnerEnd, this.u1, this.v1);
            // center
            GuiDraw.drawTexture(pose, x, y + this.bt, x1, y1 - this.bb, this.u0, vInnerStart, this.u1, vInnerEnd);
        } else {
            // top left corner
            GuiDraw.drawTexture(pose, x, y, x + this.bl, y + this.bt, this.u0, this.v0, uInnerStart, vInnerStart);
            // top right corner
            GuiDraw.drawTexture(pose, x1 - this.br, y, x1, y + this.bt, uInnerEnd, this.v0, this.u1, vInnerStart);
            // bottom left corner
            GuiDraw.drawTexture(pose, x, y1 - this.bb, x + this.bl, y1, this.u0, vInnerEnd, uInnerStart, this.v1);
            // bottom right corner
            GuiDraw.drawTexture(pose, x1 - this.br, y1 - this.bb, x1, y1, uInnerEnd, vInnerEnd, this.u1, this.v1);

            // left border
            GuiDraw.drawTexture(pose, x, y + this.bt, x + this.bl, y1 - this.bb, this.u0, vInnerStart, uInnerStart,
                    vInnerEnd);
            // top border
            GuiDraw.drawTexture(pose, x + this.bl, y, x1 - this.br, y + this.bt, uInnerStart, this.v0, uInnerEnd,
                    vInnerStart);
            // right border
            GuiDraw.drawTexture(pose, x1 - this.br, y + this.bt, x1, y1 - this.bb, uInnerEnd, vInnerStart, this.u1,
                    vInnerEnd);
            // bottom border
            GuiDraw.drawTexture(pose, x + this.bl, y1 - this.bb, x1 - this.br, y1, uInnerStart, vInnerEnd, uInnerEnd,
                    this.v1);

            // center
            GuiDraw.drawTexture(pose, x + this.bl, y + this.bt, x1 - this.br, y1 - this.bb, uInnerStart, vInnerStart,
                    uInnerEnd, vInnerEnd);
        }
        RenderSystem.disableBlend();
    }

    public void drawTiled(GuiContext context, float x, float y, float width, float height) {
        Matrix4f pose = context.getLastGraphicsPose();

        if (this.bl <= 0 && this.bt <= 0 && this.br <= 0 && this.bb <= 0) {
            GuiDraw.drawTiledTexture(pose, this.location, x, y, width, height, this.u0, this.v0, this.u1, this.v1,
                    this.imageWidth, this.imageHeight, 0);
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.location);

        float uBl = this.bl * 1f / this.imageWidth, uBr = this.br * 1f / this.imageWidth;
        float vBt = this.bt * 1f / this.imageHeight, vBb = this.bb * 1f / this.imageHeight;
        float x1 = x + width, y1 = y + height;
        float uInnerStart = this.u0 + uBl, vInnerStart = this.v0 + vBt, uInnerEnd = this.u1 - uBr,
                vInnerEnd = this.v1 - vBb;

        int tw = (int) (this.imageWidth * (this.u1 - this.u0));
        int th = (int) (this.imageHeight * (this.v1 - this.v0));

        if ((this.bl > 0 || this.br > 0) && this.bt <= 0 && this.bb <= 0) {
            // left border
            GuiDraw.drawTiledTexture(pose, x, y, this.bl, height, this.u0, this.v0, uInnerStart, this.v1, this.bl, th,
                    0);
            // right border
            GuiDraw.drawTiledTexture(pose, x1 - this.br, y, this.br, height, uInnerEnd, this.v0, this.u1, this.v1,
                    this.br, th, 0);
            // center
            GuiDraw.drawTiledTexture(pose, x + this.bl, y, width - this.bl - this.br, height, uInnerStart, this.v0,
                    uInnerEnd, this.v1, tw - this.bl - this.br, th, 0);
        } else if (this.bl <= 0 && this.br <= 0) {
            // top border
            GuiDraw.drawTiledTexture(pose, x, y, width, this.bt, this.u0, this.v0, this.u1, vInnerStart, tw, this.bt,
                    0);
            // bottom border
            GuiDraw.drawTiledTexture(pose, x, y1 - this.bb, width, this.bb, this.u0, vInnerEnd, this.u1, this.v1, tw,
                    this.bb, 0);
            // center
            GuiDraw.drawTiledTexture(pose, x, y + this.bt, width, height - this.bt - this.bb, this.u0, vInnerStart,
                    this.u1, vInnerEnd, tw, th - this.bt - this.bb, 0);
        } else {
            // top left corner
            GuiDraw.drawTiledTexture(pose, x, y, this.bl, this.bt, this.u0, this.v0, uInnerStart, vInnerStart, this.bl,
                    this.bt, 0);
            // top right corner
            GuiDraw.drawTiledTexture(pose, x1 - this.br, y, this.br, this.bt, uInnerEnd, this.v0, this.u1, vInnerStart,
                    this.br, this.bt, 0);
            // bottom left corner
            GuiDraw.drawTiledTexture(pose, x, y1 - this.bb, this.bl, this.bb, this.u0, vInnerEnd, uInnerStart, this.v1,
                    this.bl, this.bb, 0);
            // bottom right corner
            GuiDraw.drawTiledTexture(pose, x1 - this.br, y1 - this.bb, this.br, this.bb, uInnerEnd, vInnerEnd, this.u1,
                    this.v1, this.br, this.bb, 0);

            // left border
            GuiDraw.drawTiledTexture(pose, x, y + this.bt, this.bl, height - this.bt - this.bb, this.u0, vInnerStart,
                    uInnerStart, vInnerEnd, this.bl, th - this.bt - this.bb, 0);
            // top border
            GuiDraw.drawTiledTexture(pose, x + this.bl, y, width - this.bl - this.br, this.bt, uInnerStart, this.v0,
                    uInnerEnd, vInnerStart, tw - this.bl - this.bb, this.bt, 0);
            // right border
            GuiDraw.drawTiledTexture(pose, x1 - this.br, y + this.bt, this.br, height - this.bt - this.bb, uInnerEnd,
                    vInnerStart, this.u1, vInnerEnd, this.br, th - this.bt - this.bb, 0);
            // bottom border
            GuiDraw.drawTiledTexture(pose, x + this.bl, y1 - this.bb, width - this.bl - this.br, this.bb, uInnerStart,
                    vInnerEnd, uInnerEnd, this.v1, tw - this.bl - this.br, this.bb, 0);

            // center
            GuiDraw.drawTiledTexture(pose, x + this.bl, y + this.bt, width - this.bl - this.br,
                    height - this.bt - this.bb, uInnerStart, vInnerStart, uInnerEnd, vInnerEnd,
                    tw - this.bl - this.br, th - this.bt - this.bb, 0);
        }
        RenderSystem.disableBlend();
    }

    @Override
    public boolean saveToJson(JsonObject json) {
        super.saveToJson(json);
        if (json.entrySet().size() == 1) return true;
        json.addProperty("imageWidth", this.imageWidth);
        json.addProperty("imageHeight", this.imageHeight);
        json.addProperty("bl", this.bl);
        json.addProperty("br", this.br);
        json.addProperty("bt", this.bt);
        json.addProperty("bb", this.bb);
        json.addProperty("tiled", this.tiled);
        return true;
    }
}
