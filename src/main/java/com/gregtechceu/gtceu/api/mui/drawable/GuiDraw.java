package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.api.mui.drawable.text.TextRenderer;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;
import com.gregtechceu.gtceu.client.mui.screen.event.RichTooltipEvent;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import java.util.List;

public class GuiDraw {

    private static final TextRenderer textRenderer = new TextRenderer();

    public static final double TWO_PI = Math.PI * 2;
    public static final double HALF_PI = Math.PI / 2;

    public static void drawRect(GuiGraphics graphics, float x0, float y0, float w, float h, int color) {
        Matrix4f pose = graphics.pose().last().pose();
        VertexConsumer bufferbuilder = graphics.bufferSource().getBuffer(RenderType.guiOverlay());

        int r = Color.getRed(color);
        int g = Color.getGreen(color);
        int b = Color.getBlue(color);
        int a = Color.getAlpha(color);
        if (a == 0 && color != 0) a = 0xFF;

        float x1 = x0 + w, y1 = y0 + h;
        bufferbuilder.vertex(pose, x0, y0, 0.0f).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(pose, x0, y1, 0.0f).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(pose, x1, y1, 0.0f).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(pose, x1, y0, 0.0f).color(r, g, b, a).endVertex();
    }

    public static void drawHorizontalGradientRect(GuiGraphics graphics, float x0, float y0, float w, float h,
                                                  int colorLeft,
                                                  int colorRight) {
        drawRect(graphics, x0, y0, w, h, colorLeft, colorRight, colorLeft, colorRight);
    }

    public static void drawVerticalGradientRect(GuiGraphics graphics, float x0, float y0, float w, float h,
                                                int colorTop, int colorBottom) {
        drawRect(graphics, x0, y0, w, h, colorTop, colorTop, colorBottom, colorBottom);
    }

    public static void drawRect(GuiGraphics graphics, float x0, float y0, float w, float h, int colorTL, int colorTR,
                                int colorBL,
                                int colorBR) {
        Matrix4f pose = graphics.pose().last().pose();
        VertexConsumer bufferbuilder = graphics.bufferSource().getBuffer(RenderType.guiOverlay());

        float x1 = x0 + w, y1 = y0 + h;
        bufferbuilder.vertex(pose, x0, y0, 0.0f)
                .color(Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL))
                .endVertex();
        bufferbuilder.vertex(pose, x0, y1, 0.0f)
                .color(Color.getRed(colorBL), Color.getGreen(colorBL), Color.getBlue(colorBL), Color.getAlpha(colorBL))
                .endVertex();
        bufferbuilder.vertex(pose, x1, y1, 0.0f)
                .color(Color.getRed(colorBR), Color.getGreen(colorBR), Color.getBlue(colorBR), Color.getAlpha(colorBR))
                .endVertex();
        bufferbuilder.vertex(pose, x1, y0, 0.0f)
                .color(Color.getRed(colorTR), Color.getGreen(colorTR), Color.getBlue(colorTR), Color.getAlpha(colorTR))
                .endVertex();
    }

    public static void drawCircle(GuiGraphics graphics, float x0, float y0, float diameter, int color, int segments) {
        drawEllipse(graphics, x0, y0, diameter, diameter, color, color, segments);
    }

    public static void drawCircle(GuiGraphics graphics, float x0, float y0, float diameter, int centerColor,
                                  int outerColor, int segments) {
        drawEllipse(graphics, x0, y0, diameter, diameter, centerColor, outerColor, segments);
    }

    public static void drawEllipse(GuiGraphics graphics, float x0, float y0, float w, float h, int color,
                                   int segments) {
        drawEllipse(graphics, x0, y0, w, h, color, color, segments);
    }

    public static void drawEllipse(GuiGraphics graphics, float x0, float y0, float w, float h, int centerColor,
                                   int outerColor,
                                   int segments) {
        Matrix4f pose = graphics.pose().last().pose();
        VertexConsumer bufferbuilder = graphics.bufferSource().getBuffer(GTRenderTypes.guiOverlayTriangleFan());

        float x_2 = x0 + w / 2f, y_2 = y0 + h / 2f;
        // start at center
        bufferbuilder.vertex(pose, x_2, y_2, 0.0f)
                .color(Color.getRed(centerColor), Color.getGreen(centerColor), Color.getBlue(centerColor),
                        Color.getAlpha(centerColor))
                .endVertex();
        int a = Color.getAlpha(outerColor), r = Color.getRed(outerColor), g = Color.getGreen(outerColor),
                b = Color.getBlue(outerColor);
        float incr = (float) (TWO_PI / segments);
        for (int i = 0; i <= segments; i++) {
            float angle = incr * i;
            float x = (float) (Math.sin(angle) * (w / 2) + x_2);
            float y = (float) (Math.cos(angle) * (h / 2) + y_2);
            bufferbuilder.vertex(x, y, 0.0f).color(r, g, b, a).endVertex();
        }
        RenderSystem.disableBlend();
    }

    public static void drawRoundedRect(GuiGraphics graphics, float x0, float y0, float w, float h, int color,
                                       int cornerRadius,
                                       int segments) {
        drawRoundedRect(graphics, x0, y0, w, h, color, color, color, color, cornerRadius, segments);
    }

    public static void drawVerticalGradientRoundedRect(GuiGraphics graphics, float x0, float y0, float w, float h,
                                                       int colorTop,
                                                       int colorBottom, int cornerRadius, int segments) {
        drawRoundedRect(graphics, x0, y0, w, h, colorTop, colorTop, colorBottom, colorBottom, cornerRadius, segments);
    }

    public static void drawHorizontalGradientRoundedRect(GuiGraphics graphics, float x0, float y0, float w, float h,
                                                         int colorLeft,
                                                         int colorRight, int cornerRadius, int segments) {
        drawRoundedRect(graphics, x0, y0, w, h, colorLeft, colorRight, colorLeft, colorRight, cornerRadius, segments);
    }

    public static void drawRoundedRect(GuiGraphics graphics, float x0, float y0, float w, float h, int colorTL,
                                       int colorTR, int colorBL,
                                       int colorBR, int cornerRadius, int segments) {
        Matrix4f pose = graphics.pose().last().pose();
        VertexConsumer bufferbuilder = graphics.bufferSource().getBuffer(GTRenderTypes.guiOverlayTriangleFan());

        float x1 = x0 + w, y1 = y0 + h;
        int color = Color.average(colorBL, colorBR, colorTR, colorTL);
        // start at center
        bufferbuilder.vertex(pose, x0 + w / 2f, y0 + h / 2f, 0.0f)
                .color(Color.getRed(color), Color.getGreen(color), Color.getBlue(color), Color.getAlpha(color))
                .endVertex();
        // left side
        bufferbuilder.vertex(pose, x0, y0 + cornerRadius, 0.0f)
                .color(Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL))
                .endVertex();
        bufferbuilder.vertex(pose, x0, y1 - cornerRadius, 0.0f)
                .color(Color.getRed(colorBL), Color.getGreen(colorBL), Color.getBlue(colorBL), Color.getAlpha(colorBL))
                .endVertex();
        // bottom left corner
        for (int i = 1; i <= segments; i++) {
            float x = (float) (x0 + cornerRadius - Math.cos(HALF_PI / segments * i) * cornerRadius);
            float y = (float) (y1 - cornerRadius + Math.sin(HALF_PI / segments * i) * cornerRadius);
            bufferbuilder.vertex(x, y, 0.0f)
                    .color(Color.getRed(colorBL), Color.getGreen(colorBL), Color.getBlue(colorBL),
                            Color.getAlpha(colorBL))
                    .endVertex();
        }
        // bottom side
        bufferbuilder.vertex(pose, x1 - cornerRadius, y1, 0.0f)
                .color(Color.getRed(colorBR), Color.getGreen(colorBR), Color.getBlue(colorBR), Color.getAlpha(colorBR))
                .endVertex();
        // bottom right corner
        for (int i = 1; i <= segments; i++) {
            float x = (float) (x1 - cornerRadius + Math.sin(HALF_PI / segments * i) * cornerRadius);
            float y = (float) (y1 - cornerRadius + Math.cos(HALF_PI / segments * i) * cornerRadius);
            bufferbuilder.vertex(pose, x, y, 0.0f)
                    .color(Color.getRed(colorBR), Color.getGreen(colorBR), Color.getBlue(colorBR),
                            Color.getAlpha(colorBR))
                    .endVertex();
        }
        // right side
        bufferbuilder.vertex(pose, x1, y0 + cornerRadius, 0.0f)
                .color(Color.getRed(colorTR), Color.getGreen(colorTR), Color.getBlue(colorTR), Color.getAlpha(colorTR))
                .endVertex();
        // top right corner
        for (int i = 1; i <= segments; i++) {
            float x = (float) (x1 - cornerRadius + Math.cos(HALF_PI / segments * i) * cornerRadius);
            float y = (float) (y0 + cornerRadius - Math.sin(HALF_PI / segments * i) * cornerRadius);
            bufferbuilder.vertex(pose, x, y, 0.0f)
                    .color(Color.getRed(colorTR), Color.getGreen(colorTR), Color.getBlue(colorTR),
                            Color.getAlpha(colorTR))
                    .endVertex();
        }
        // top side
        bufferbuilder.vertex(pose, x0 + cornerRadius, y0, 0.0f)
                .color(Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL))
                .endVertex();
        // top left corner
        for (int i = 1; i <= segments; i++) {
            float x = (float) (x0 + cornerRadius - Math.sin(HALF_PI / segments * i) * cornerRadius);
            float y = (float) (y0 + cornerRadius - Math.cos(HALF_PI / segments * i) * cornerRadius);
            bufferbuilder.vertex(pose, x, y, 0.0f)
                    .color(Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL),
                            Color.getAlpha(colorTL))
                    .endVertex();
        }
        bufferbuilder.vertex(pose, x0, y0 + cornerRadius, 0.0f)
                .color(Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL))
                .endVertex();
    }

    public static void drawTexture(Matrix4f pose, ResourceLocation location, float x, float y, float w, float h, int u,
                                   int v,
                                   int textureWidth, int textureHeight) {
        RenderSystem.setShaderTexture(0, location);
        drawTexture(pose, x, y, u, v, w, h, textureWidth, textureHeight);
    }

    public static void drawTexture(Matrix4f pose, float x, float y, int u, int v, float w, float h, int textureW,
                                   int textureH) {
        drawTexture(pose, x, y, u, v, w, h, textureW, textureH, 0);
    }

    /**
     * Draw a textured quad with given UV, dimensions and custom texture size
     */
    public static void drawTexture(Matrix4f pose, float x, float y, int u, int v, float w, float h, int textureW,
                                   int textureH, float z) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        drawTexture(pose, buffer, x, y, u, v, w, h, textureW, textureH, z);
        tesselator.end();
    }

    public static void drawTexture(Matrix4f pose, VertexConsumer buffer, float x, float y, int u, int v, float w,
                                   float h, int textureW,
                                   int textureH, float z) {
        float tw = 1F / textureW;
        float th = 1F / textureH;

        buffer.vertex(pose, x, y + h, z).uv(u * tw, (v + h) * th).endVertex();
        buffer.vertex(pose, x + w, y + h, z).uv((u + w) * tw, (v + h) * th).endVertex();
        buffer.vertex(pose, x + w, y, z).uv((u + w) * tw, v * th).endVertex();
        buffer.vertex(pose, x, y, z).uv(u * tw, v * th).endVertex();
    }

    public static void drawTexture(Matrix4f pose, float x, float y, int u, int v, float w, float h, int textureW,
                                   int textureH, int tu,
                                   int tv) {
        drawTexture(pose, x, y, u, v, w, h, textureW, textureH, tu, tv, 0);
    }

    /**
     * Draw a textured quad with given UV, dimensions and custom texture size
     */
    public static void drawTexture(Matrix4f pose, float x, float y, int u, int v, float w, float h, int textureW,
                                   int textureH, int tu,
                                   int tv, float z) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        drawTexture(pose, buffer, x, y, u, v, w, h, textureW, textureH, tu, tv, z);
        tesselator.end();
    }

    public static void drawTexture(Matrix4f pose, VertexConsumer buffer, float x, float y, int u, int v, float w,
                                   float h, int textureW,
                                   int textureH, int tu, int tv, float z) {
        float tw = 1F / textureW;
        float th = 1F / textureH;

        buffer.vertex(pose, x, y + h, z).uv(u * tw, tv * th).endVertex();
        buffer.vertex(pose, x + w, y + h, z).uv(tu * tw, tv * th).endVertex();
        buffer.vertex(pose, x + w, y, z).uv(tu * tw, v * th).endVertex();
        buffer.vertex(pose, x, y, z).uv(u * tw, v * th).endVertex();
    }

    public static void drawTexture(Matrix4f pose, ResourceLocation location, float x0, float y0, float x1, float y1,
                                   float u0, float v0,
                                   float u1, float v1) {
        RenderSystem.setShaderTexture(0, location);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        drawTexture(pose, x0, y0, x1, y1, u0, v0, u1, v1, 0);
    }

    public static void drawTexture(Matrix4f pose, float x0, float y0, float x1, float y1, float u0, float v0, float u1,
                                   float v1) {
        drawTexture(pose, x0, y0, x1, y1, u0, v0, u1, v1, 0);
    }

    public static void drawTexture(Matrix4f pose, float x0, float y0, float x1, float y1, float u0, float v0, float u1,
                                   float v1, float z) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        drawTexture(pose, buffer, x0, y0, x1, y1, u0, v0, u1, v1, z);
        tesselator.end();
    }

    public static void drawTexture(Matrix4f pose, VertexConsumer buffer, float x0, float y0, float x1, float y1,
                                   float u0, float v0,
                                   float u1, float v1, float z) {
        buffer.vertex(pose, x0, y1, z).uv(u0, v1).endVertex();
        buffer.vertex(pose, x1, y1, z).uv(u1, v1).endVertex();
        buffer.vertex(pose, x1, y0, z).uv(u1, v0).endVertex();
        buffer.vertex(pose, x0, y0, z).uv(u0, v0).endVertex();
    }

    public static void drawTiledTexture(Matrix4f pose, ResourceLocation location, float x, float y, float w, float h,
                                        int u, int v,
                                        int tileW, int tileH, int tw, int th, float z) {
        RenderSystem.setShaderTexture(0, location);
        drawTiledTexture(pose, x, y, w, h, u, v, tileW, tileH, tw, th, z);
    }

    public static void drawTiledTexture(Matrix4f pose, float x, float y, float w, float h, int u, int v, int tileW,
                                        int tileH, int tw,
                                        int th, float z) {
        int countX = (((int) w - 1) / tileW) + 1;
        int countY = (((int) h - 1) / tileH) + 1;
        float fillerX = w - (countX - 1) * tileW;
        float fillerY = h - (countY - 1) * tileH;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        for (int i = 0, c = countX * countY; i < c; i++) {
            int ix = i % countX;
            int iy = i / countX;
            float xx = x + ix * tileW;
            float yy = y + iy * tileH;
            float xw = ix == countX - 1 ? fillerX : tileW;
            float yh = iy == countY - 1 ? fillerY : tileH;

            drawTexture(pose, buffer, xx, yy, u, v, xw, yh, tw, th, z);
        }
    }

    public static void drawTiledTexture(Matrix4f pose, ResourceLocation location, float x, float y, float w, float h,
                                        float u0, float v0,
                                        float u1, float v1, int textureWidth, int textureHeight, float z) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, location);
        drawTiledTexture(pose, x, y, w, h, u0, v0, u1, v1, textureWidth, textureHeight, z);
        RenderSystem.disableBlend();
    }

    public static void drawTiledTexture(Matrix4f pose, float x, float y, float w, float h, float u0, float v0, float u1,
                                        float v1,
                                        int tileWidth, int tileHeight, float z) {
        int countX = (((int) w - 1) / tileWidth) + 1;
        int countY = (((int) h - 1) / tileHeight) + 1;
        float fillerX = w - (countX - 1) * tileWidth;
        float fillerY = h - (countY - 1) * tileHeight;
        float fillerU = u0 + (u1 - u0) * fillerX / tileWidth;
        float fillerV = v0 + (v1 - v0) * fillerY / tileHeight;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        for (int i = 0, c = countX * countY; i < c; i++) {
            int ix = i % countX;
            int iy = i / countX;
            float xx = x + ix * tileWidth;
            float yy = y + iy * tileHeight;
            float xw = tileWidth, yh = tileHeight, uEnd = u1, vEnd = v1;
            if (ix == countX - 1) {
                xw = fillerX;
                uEnd = fillerU;
            }
            if (iy == countY - 1) {
                yh = fillerY;
                vEnd = fillerV;
            }

            drawTexture(pose, buffer, xx, yy, xx + xw, yy + yh, u0, v0, uEnd, vEnd, z);
        }

        tesselator.end();
    }

    public static void drawLivingEntity(GuiGraphics graphics, LivingEntity entity, int x, int y, float width,
                                        float height, int z) {
        int scale = 132;
        Quaternionf pose = new Quaternionf(1.414f, 0.0f, 1.0f, 0.0f);
        graphics.pose().pushPose();
        graphics.pose().translate((double) x + width / 2, (double) y + height, 50.0D);
        graphics.pose()
                .mulPoseMatrix((new Matrix4f()).scaling((float) width / 2, (float) height / 2, (float) (-scale)));
        graphics.pose().mulPose(pose);
        Lighting.setupForEntityInInventory();

        EntityRenderDispatcher erd = Minecraft.getInstance().getEntityRenderDispatcher();
        erd.setRenderShadow(false);

        RenderSystem.runAsFancy(() -> {
            erd.render(entity, 0.0d, 0.0d, 0.0d, 0.0f, 1.0f, graphics.pose(), graphics.bufferSource(),
                    LightTexture.FULL_BRIGHT);
        });
        graphics.flush();
        erd.setRenderShadow(true);
        graphics.pose().popPose();
        Lighting.setupFor3DItems();
    }

    public static void drawItem(GuiGraphics graphics, ItemStack item, int x, int y, float width, float height, int z) {
        if (item.isEmpty()) return;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, z);
        graphics.pose().scale(width / 16f, height / 16f, 1);
        graphics.renderItem(item, 0, 0);
        graphics.renderItemDecorations(Minecraft.getInstance().font, item, 0, 0);
        graphics.pose().popPose();
    }

    public static void drawFluidTexture(GuiGraphics graphics, FluidStack content, float x0, float y0, float width,
                                        float height, float z) {
        if (content == null || content.isEmpty()) {
            return;
        }
        Fluid fluid = content.getFluid();
        ResourceLocation fluidStill = IClientFluidTypeExtensions.of(fluid).getStillTexture(content);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(fluidStill);
        int fluidColor = IClientFluidTypeExtensions.of(fluid).getTintColor(content);
        graphics.setColor(Color.getRedF(fluidColor), Color.getGreenF(fluidColor), Color.getBlueF(fluidColor),
                Color.getAlphaF(fluidColor));
        drawTiledTexture(graphics.pose().last().pose(), InventoryMenu.BLOCK_ATLAS, x0, y0, width, height,
                sprite.getU0(), sprite.getV0(),
                sprite.getU1(), sprite.getV1(), sprite.contents().width(), sprite.contents().height(), z);
        graphics.setColor(1f, 1f, 1f, 1f);
    }

    public static void drawStandardSlotAmountText(ModularGuiContext context, int amount, String format, Area area,
                                                  float z) {
        drawAmountText(context, amount, format, 1, 1, area.width - 1, area.height - 1, Alignment.BottomRight, z);
    }

    public static void drawAmountText(ModularGuiContext context, int amount, String format, int x, int y, int width,
                                      int height,
                                      Alignment alignment, float z) {
        // render the amount overlay
        if (amount > 1 || format != null) {
            String amountText = FormattingUtil.formatNumberReadable(amount, false);
            if (format != null) {
                amountText = format + amountText;
            }
            float scale = 1f;
            if (amountText.length() == 3) {
                scale = 0.8f;
            } else if (amountText.length() == 4) {
                scale = 0.6f;
            } else if (amountText.length() > 4) {
                scale = 0.5f;
            }
            textRenderer.setShadow(true);
            textRenderer.setScale(scale);
            textRenderer.setColor(Color.WHITE.main);
            textRenderer.setAlignment(alignment, width, height);
            textRenderer.setPos(x, y);
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
            context.graphicsPose().translate(0, 0, 100 + z);
            textRenderer.draw(context.getGraphics(), amountText);
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
        }
    }

    public static void drawSprite(Matrix4f pose, TextureAtlasSprite sprite, float x0, float y0, float w, float h) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
        drawTexture(pose, x0, y0, x0 + w, y0 + h, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
        RenderSystem.disableBlend();
    }

    public static void drawTiledSprite(Matrix4f pose, TextureAtlasSprite sprite, float x0, float y0, float w, float h) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
        drawTiledTexture(pose, sprite.atlasLocation(), x0, y0, x0 + w, y0 + h, sprite.getU0(), sprite.getV0(),
                sprite.getU1(),
                sprite.getV1(), sprite.contents().width(), sprite.contents().height(), 0);
        RenderSystem.disableBlend();
    }

    public static void drawOutlineCenter(GuiGraphics graphics, int x, int y, int offset, int color) {
        drawOutlineCenter(graphics, x, y, offset, color, 1);
    }

    public static void drawOutlineCenter(GuiGraphics graphics, int x, int y, int offset, int color, int border) {
        drawOutline(graphics, x - offset, y - offset, x + offset, y + offset, color, border);
    }

    public static void drawOutline(GuiGraphics graphics, int left, int top, int right, int bottom, int color) {
        drawOutline(graphics, left, top, right, bottom, color, 1);
    }

    /**
     * Draw rectangle outline with given border
     */
    public static void drawOutline(GuiGraphics graphics, int left, int top, int right, int bottom, int color,
                                   int border) {
        graphics.fill(left, top, left + border, bottom, color);
        graphics.fill(right - border, top, right, bottom, color);
        graphics.fill(left + border, top, right - border, top + border, color);
        graphics.fill(left + border, bottom - border, right - border, bottom, color);
    }

    /**
     * Draws a rectangular shadow
     *
     * @param x      left of solid shadow part
     * @param y      top of solid shadow part
     * @param w      width of solid shadow part
     * @param h      height of solid shadow part
     * @param oX     shadow gradient size in x
     * @param oY     shadow gradient size in y
     * @param opaque solid shadow color
     * @param shadow gradient end color
     */
    public static void drawDropShadow(Matrix4f pose, int x, int y, int w, int h, int oX, int oY, int opaque,
                                      int shadow) {
        float a1 = Color.getAlphaF(opaque);
        float r1 = Color.getRedF(opaque);
        float g1 = Color.getGreenF(opaque);
        float b1 = Color.getBlueF(opaque);
        float a2 = Color.getAlphaF(shadow);
        float r2 = Color.getRedF(shadow);
        float g2 = Color.getGreenF(shadow);
        float b2 = Color.getBlueF(shadow);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float x1 = x + w, y1 = y + h;

        /* Draw opaque part */
        buffer.vertex(pose, x1, y, 0).color(r1, g1, b1, a1).endVertex();
        buffer.vertex(pose, x, y, 0).color(r1, g1, b1, a1).endVertex();
        buffer.vertex(pose, x, y1, 0).color(r1, g1, b1, a1).endVertex();
        buffer.vertex(pose, x1, y1, 0).color(r1, g1, b1, a1).endVertex();

        /* Draw top shadow */
        buffer.vertex(pose, x1 + oX, y - oY, 0).color(r2, g2, b2, a2).endVertex();
        buffer.vertex(pose, x - oX, y - oY, 0).color(r2, g2, b2, a2).endVertex();
        buffer.vertex(pose, x, y, 0).color(r1, g1, b1, a1).endVertex();
        buffer.vertex(pose, x1, y, 0).color(r1, g1, b1, a1).endVertex();

        /* Draw bottom shadow */
        buffer.vertex(pose, x1, y1, 0).color(r1, g1, b1, a1).endVertex();
        buffer.vertex(pose, x, y1, 0).color(r1, g1, b1, a1).endVertex();
        buffer.vertex(pose, x - oX, y1 + oY, 0).color(r2, g2, b2, a2).endVertex();
        buffer.vertex(pose, x1 + oX, y1 + oY, 0).color(r2, g2, b2, a2).endVertex();

        /* Draw left shadow */
        buffer.vertex(pose, x, y, 0).color(r1, g1, b1, a1).endVertex();
        buffer.vertex(pose, x - oX, y - oY, 0).color(r2, g2, b2, a2).endVertex();
        buffer.vertex(pose, x - oX, y1 + oY, 0).color(r2, g2, b2, a2).endVertex();
        buffer.vertex(pose, x, y1, 0).color(r1, g1, b1, a1).endVertex();

        /* Draw right shadow */
        buffer.vertex(pose, x1 + oX, y - oY, 0).color(r2, g2, b2, a2).endVertex();
        buffer.vertex(pose, x1, y, 0).color(r1, g1, b1, a1).endVertex();
        buffer.vertex(pose, x1, y1, 0).color(r1, g1, b1, a1).endVertex();
        buffer.vertex(pose, x1 + oX, y1 + oY, 0).color(r2, g2, b2, a2).endVertex();

        tesselator.end();
        RenderSystem.disableBlend();
    }

    public static void drawDropCircleShadow(GuiGraphics graphics, int x, int y, int radius, int segments, int opaque,
                                            int shadow) {
        Matrix4f pose = graphics.pose().last().pose();
        Matrix4d poseD = new Matrix4d(pose);

        float a1 = Color.getAlphaF(opaque);
        float r1 = Color.getRedF(opaque);
        float g1 = Color.getGreenF(opaque);
        float b1 = Color.getBlueF(opaque);
        float a2 = Color.getAlphaF(shadow);
        float r2 = Color.getRedF(shadow);
        float g2 = Color.getGreenF(shadow);
        float b2 = Color.getBlueF(shadow);

        VertexConsumer buffer = graphics.bufferSource().getBuffer(GTRenderTypes.guiTriangleFan());
        buffer.vertex(pose, x, y, 0).color(r1, g1, b1, a1).endVertex();

        Vector3d pos = new Vector3d();
        for (int i = 0; i <= segments; i++) {
            double a = i / (double) segments * TWO_PI - HALF_PI;
            circleVertex(buffer, poseD, pos, x, Math.cos(a), y, Math.sin(a), radius).color(r2, g2, b2, a2).endVertex();
        }
    }

    public static void drawDropCircleShadow(GuiGraphics graphics, int x, int y, int radius, int offset, int segments,
                                            int opaque,
                                            int shadow) {
        if (offset >= radius) {
            drawDropCircleShadow(graphics, x, y, radius, segments, opaque, shadow);
            return;
        }
        Matrix4f pose = graphics.pose().last().pose();
        Matrix4d poseD = new Matrix4d(pose);

        float a1 = Color.getAlphaF(opaque);
        float r1 = Color.getRedF(opaque);
        float g1 = Color.getGreenF(opaque);
        float b1 = Color.getBlueF(opaque);
        float a2 = Color.getAlphaF(shadow);
        float r2 = Color.getRedF(shadow);
        float g2 = Color.getGreenF(shadow);
        float b2 = Color.getBlueF(shadow);

        VertexConsumer buffer = graphics.bufferSource().getBuffer(GTRenderTypes.guiTriangleFan());
        /* Draw opaque base */
        buffer.vertex(pose, x, y, 0).color(r1, g1, b1, a1).endVertex();

        Vector3d pos = new Vector3d();
        for (int i = 0; i <= segments; i++) {
            double a = i / (double) segments * TWO_PI - HALF_PI;
            circleVertex(buffer, poseD, pos, x, Math.cos(a), y, Math.sin(a), offset).color(r1, g1, b1, a1).endVertex();
        }

        /* Draw outer shadow */
        buffer = graphics.bufferSource().getBuffer(RenderType.gui());

        for (int i = 0; i < segments; i++) {
            double alpha1 = i / (double) segments * TWO_PI - HALF_PI;
            double alpha2 = (i + 1) / (double) segments * TWO_PI - HALF_PI;

            double cosA1 = Math.cos(alpha1);
            double cosA2 = Math.cos(alpha2);
            double sinA1 = Math.sin(alpha1);
            double sinA2 = Math.sin(alpha2);

            circleVertex(buffer, poseD, pos, x, cosA2, y, sinA2, offset).color(r1, g1, b1, a1).endVertex();
            circleVertex(buffer, poseD, pos, x, cosA1, y, sinA1, offset).color(r1, g1, b1, a1).endVertex();
            circleVertex(buffer, poseD, pos, x, cosA1, y, sinA1, radius).color(r2, g2, b2, a2).endVertex();
            circleVertex(buffer, poseD, pos, x, cosA2, y, sinA2, radius).color(r2, g2, b2, a2).endVertex();
        }
    }

    private static VertexConsumer circleVertex(VertexConsumer buffer, Matrix4d pose, Vector3d pos, double x,
                                               double xOffset, double y,
                                               double yOffset, double mul) {
        pos.x = x - xOffset * mul;
        pos.y = y + yOffset * mul;
        pose.transformPosition(pos);
        return buffer.vertex(pos.x, pos.y, pos.z);
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawBorder(GuiGraphics graphics, float x, float y, float width, float height, int color,
                                  float border) {
        drawRect(graphics, x - border, y - border, width + 2 * border, border, color);
        drawRect(graphics, x - border, y + height, width + 2 * border, border, color);
        drawRect(graphics, x - border, y, border, height, color);
        drawRect(graphics, x + width, y, border, height, color);
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawText(GuiGraphics graphics, String text, float x, float y, float scale, int color,
                                boolean shadow) {
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 0f);
        float sf = 1 / scale;
        graphics.drawString(Minecraft.getInstance().font, text, x * sf, y * sf, color, shadow);
        graphics.pose().popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawText(GuiGraphics graphics, Component text, float x, float y, float scale, int color,
                                boolean shadow) {
        drawText(graphics, text.getVisualOrderText(), x, y, scale, color, shadow);
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawText(GuiGraphics graphics, FormattedCharSequence text, float x, float y, float scale,
                                int color,
                                boolean shadow) {
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 0f);
        float sf = 1 / scale;
        graphics.drawString(Minecraft.getInstance().font, text, x * sf, y * sf, color, shadow);
        graphics.pose().popPose();
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void drawTooltipBackground(GuiContext context, ItemStack stack, List<ClientTooltipComponent> lines,
                                             int x, int y, int textWidth, int height, @Nullable RichTooltip tooltip) {
        GuiGraphics graphics = context.getGraphics();

        // TODO theme color
        int backgroundTop = 0xF0100010;
        int backgroundBottom = backgroundTop;
        int borderColorStart = 0x505000FF;
        int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
        RenderTooltipEvent.Color colorEvent;

        if (tooltip != null) {
            colorEvent = new RichTooltipEvent.Color(stack, graphics, x, y, context.getFont(),
                    backgroundTop, borderColorStart, borderColorEnd, lines, tooltip);
        } else {
            colorEvent = new RenderTooltipEvent.Color(stack, graphics, x, y, context.getFont(),
                    backgroundTop, borderColorStart, borderColorEnd, lines);
        }

        MinecraftForge.EVENT_BUS.post(colorEvent);
        backgroundTop = colorEvent.getBackgroundStart();
        backgroundBottom = colorEvent.getBackgroundEnd();
        borderColorStart = colorEvent.getBorderStart();
        borderColorEnd = colorEvent.getBorderEnd();

        // top background border
        drawVerticalGradientRect(graphics, x - 3, y - 4, textWidth + 6, 1, backgroundTop, backgroundTop);
        // bottom background border
        drawVerticalGradientRect(graphics, x - 3, y + height + 3, textWidth + 6, 1, backgroundBottom, backgroundBottom);
        // center background
        drawVerticalGradientRect(graphics, x - 3, y - 3, textWidth + 6, height + 6, backgroundTop, backgroundBottom);
        // left background border
        drawVerticalGradientRect(graphics, x - 4, y - 3, 1, height + 6, backgroundTop, backgroundBottom);
        // right background border
        drawVerticalGradientRect(graphics, x + textWidth + 3, y - 3, 1, height + 6, backgroundTop, backgroundBottom);

        // left accent border
        drawVerticalGradientRect(graphics, x - 3, y - 2, 1, height + 4, borderColorStart, borderColorEnd);
        // right accent border
        drawVerticalGradientRect(graphics, x + textWidth + 2, y - 2, 1, height + 4, borderColorStart, borderColorEnd);
        // top accent border
        drawVerticalGradientRect(graphics, x - 3, y - 3, textWidth + 6, 1, borderColorStart, borderColorStart);
        // bottom accent border
        drawVerticalGradientRect(graphics, x - 3, y + height + 2, textWidth + 6, 1, borderColorEnd, borderColorEnd);
    }
}
