package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.core.mixins.GuiGraphicsAccessor;

import com.lowdragmc.lowdraglib.utils.ColorUtils;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

public class DrawUtil {

    /**
     * Fills a rectangle with a gradient color from colorFrom to colorTo at the specified z-level using the given render
     * type and coordinates as the boundaries.
     *
     * @param y2         the y-coordinate of the second corner of the rectangle.
     * @param x2         the x-coordinate of the second corner of the rectangle.
     * @param y1         the y-coordinate of the first corner of the rectangle.
     * @param x1         the x-coordinate of the first corner of the rectangle.
     * @param renderType the render type to use.
     * @param z          the z-level of the rectangle.
     * @param colorTo    the ending color of the gradient.
     * @param colorFrom  the starting color of the gradient.
     */
    public static void fillHorizontalGradient(GuiGraphics graphics, RenderType renderType, int x1, int y1, int x2,
                                              int y2, int colorFrom, int colorTo, int z) {
        VertexConsumer vertexconsumer = graphics.bufferSource().getBuffer(renderType);
        fillHorizontalGradient(graphics, vertexconsumer, x1, y1, x2, y2, z, colorFrom, colorTo);
        ((GuiGraphicsAccessor) graphics).callFlushIfUnmanaged();
    }

    /**
     * The core `fillGradient` method.
     * <p>
     * Fills a rectangle with a gradient color from colorFrom to colorTo at the specified z-level using the given render
     * type and coordinates as the boundaries.
     *
     * @param consumer  the {@linkplain VertexConsumer} object for drawing the vertices on screen.
     * @param x1        the x-coordinate of the first corner of the rectangle.
     * @param y1        the y-coordinate of the first corner of the rectangle.
     * @param x2        the x-coordinate of the second corner of the rectangle.
     * @param y2        the y-coordinate of the second corner of the rectangle.
     * @param z         the z-level of the rectangle.
     * @param colorFrom the starting color of the gradient.
     * @param colorTo   the ending color of the gradient.
     */
    private static void fillHorizontalGradient(GuiGraphics graphics, VertexConsumer consumer, int x1, int y1, int x2,
                                               int y2, int z, int colorFrom, int colorTo) {
        float a1 = ColorUtils.alpha(colorFrom);
        float r1 = ColorUtils.red(colorFrom);
        float g1 = ColorUtils.green(colorFrom);
        float b1 = ColorUtils.blue(colorFrom);
        float a2 = ColorUtils.alpha(colorTo);
        float r2 = ColorUtils.red(colorTo);
        float g2 = ColorUtils.green(colorTo);
        float b2 = ColorUtils.blue(colorTo);
        Matrix4f matrix4f = graphics.pose().last().pose();
        consumer.vertex(matrix4f, (float) x1, (float) y1, (float) z).color(r1, g1, b1, a1).endVertex();
        consumer.vertex(matrix4f, (float) x1, (float) y2, (float) z).color(r1, g1, b1, a1).endVertex();
        consumer.vertex(matrix4f, (float) x2, (float) y2, (float) z).color(r2, g2, b2, a2).endVertex();
        consumer.vertex(matrix4f, (float) x2, (float) y1, (float) z).color(r2, g2, b2, a2).endVertex();
    }

    public static int interpolateColor(int color1, int color2, float blend) {
        int a1 = color1 >> 24 & 255;
        int r1 = color1 >> 16 & 255;
        int g1 = color1 >> 8 & 255;
        int b1 = color1 & 255;

        int a2 = color2 >> 24 & 255;
        int r2 = color2 >> 16 & 255;
        int g2 = color2 >> 8 & 255;
        int b2 = color2 & 255;

        int a = (int) (a1 * (1 - blend) + a2 * blend);
        int r = (int) (r1 * (1 - blend) + r2 * blend);
        int g = (int) (g1 * (1 - blend) + g2 * blend);
        int b = (int) (b1 * (1 - blend) + b2 * blend);
        return a << 24 | r << 16 | g << 8 | b;
    }
}
