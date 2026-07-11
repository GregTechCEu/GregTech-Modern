package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.api.recipe.ingredient.IChancedIngredient;
import com.gregtechceu.gtceu.core.mixins.client.GuiGraphicsAccessor;

import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GradientUtil;
import com.lowdragmc.lowdraglib.utils.ColorUtils;

import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
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

    /**
     * Converts an (A)RGB integer color into an array of floats, for use in GL calls
     * 
     * @return float[]{R, G, B, A}
     */
    public static float[] floats(int argb) {
        return new float[] {
                (float) (argb >> 16 & 255) / 255.0F,
                (float) (argb >> 8 & 255) / 255.0F,
                (float) (argb & 255) / 255.0F,
                (float) (argb >> 24 & 255) / 255.0F
        };
    }

    public static void drawChance(GuiGraphics graphics, float x, float y, int width, int height, int chance) {
        if (chance == IChancedIngredient.MAX_CHANCE) return;
        float chanceFloat = 1f * chance / IChancedIngredient.MAX_CHANCE;
        String s = chance == 0 ? LocalizationUtils.format("gtceu.gui.content.chance_nc_short") :
                FormattingUtil.formatNumber2Places(100 * chanceFloat) + "%";
        int color = chance == 0 ? 0xFF0000 : GradientUtil.toRGB(Mth.lerp(chanceFloat, 29f, 167f), 100f, 50f);
        drawString(graphics, x, y, width, height, s, color, true);
    }

    public static void drawString(GuiGraphics graphics, float x, float y, int width, int height, String s, int color,
                                   boolean top) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
        graphics.pose().scale(0.5f, 0.5f, 1);
        Font fontRenderer = Minecraft.getInstance().font;
        graphics.drawString(fontRenderer, s, (int) ((x + (width / 3f)) * 2 - fontRenderer.width(s) + 23),
                (int) ((y + (height / 3f) + 6) * 2 - (top ? height + 5 : 0)), color, true);
        graphics.pose().popPose();
    }
}
