package com.gregtechceu.gtceu.client.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

import com.mojang.blaze3d.pipeline.RenderPipeline;

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
    public static void fillHorizontalGradient(GuiGraphicsExtractor graphics, RenderPipeline pipeline, int x1, int y1,
                                              int x2,
                                              int y2, int colorFrom, int colorTo, int z) {
        int width = x2 - x1;
        if (width <= 0) {
            return;
        }

        for (int x = x1; x < x2; x++) {
            float delta = width == 1 ? 0.0f : (float) (x - x1) / (width - 1);
            graphics.fill(pipeline, x, y1, x + 1, y2, lerpColor(delta, colorFrom, colorTo));
        }
    }

    private static int lerpColor(float delta, int colorFrom, int colorTo) {
        int alpha = Mth.lerpInt(delta, ARGB.alpha(colorFrom), ARGB.alpha(colorTo));
        int red = Mth.lerpInt(delta, ARGB.red(colorFrom), ARGB.red(colorTo));
        int green = Mth.lerpInt(delta, ARGB.green(colorFrom), ARGB.green(colorTo));
        int blue = Mth.lerpInt(delta, ARGB.blue(colorFrom), ARGB.blue(colorTo));
        return ARGB.color(alpha, red, green, blue);
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
}
