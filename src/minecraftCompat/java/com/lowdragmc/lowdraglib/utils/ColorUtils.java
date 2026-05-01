package com.lowdragmc.lowdraglib.utils;

import net.minecraft.util.Mth;

import java.util.concurrent.ThreadLocalRandom;

public class ColorUtils {

    public static int randomColor(int rMin, int rMax, int gMin, int gMax, int bMin, int bMax) {
        return randomColor(255, 255, rMin, rMax, gMin, gMax, bMin, bMax);
    }

    public static int randomColor(int aMin, int aMax, int rMin, int rMax, int gMin, int gMax, int bMin, int bMax) {
        var random = ThreadLocalRandom.current();
        return color(random.nextInt(aMin, aMax + 1), random.nextInt(rMin, rMax + 1),
                random.nextInt(gMin, gMax + 1), random.nextInt(bMin, bMax + 1));
    }

    public static int randomColor(int min, int max) {
        return randomColor(255, 255, min, max, min, max, min, max);
    }

    public static int randomColor() {
        return randomColor(0, 255);
    }

    public static int averageColor(int... colors) {
        if (colors.length == 0) return 0;
        int a = 0;
        int r = 0;
        int g = 0;
        int b = 0;
        for (int color : colors) {
            a += alphaI(color);
            r += redI(color);
            g += greenI(color);
            b += blueI(color);
        }
        return color(a / colors.length, r / colors.length, g / colors.length, b / colors.length);
    }

    public static double softLightBlend(double base, double blend, double opacity, double alpha) {
        double value = blend < 0.5 ? 2 * base * blend + base * base * (1 - 2 * blend) :
                Math.sqrt(base) * (2 * blend - 1) + 2 * base * (1 - blend);
        return Mth.lerp(opacity * alpha, base, value);
    }

    public static float alpha(int color) {
        return alphaI(color) / 255.0f;
    }

    public static float red(int color) {
        return redI(color) / 255.0f;
    }

    public static float green(int color) {
        return greenI(color) / 255.0f;
    }

    public static float blue(int color) {
        return blueI(color) / 255.0f;
    }

    public static int alphaI(int color) {
        return color >>> 24 & 0xFF;
    }

    public static int redI(int color) {
        return color >>> 16 & 0xFF;
    }

    public static int greenI(int color) {
        return color >>> 8 & 0xFF;
    }

    public static int blueI(int color) {
        return color & 0xFF;
    }

    public static int color(int alpha, int red, int green, int blue) {
        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }

    public static int color(float alpha, float red, float green, float blue) {
        return color((int) (alpha * 255), (int) (red * 255), (int) (green * 255), (int) (blue * 255));
    }

    public static int HSBtoRGB(float hue, float saturation, float brightness, float alpha) {
        return color((int) (alpha * 255), java.awt.Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF);
    }

    private static int color(int alpha, int rgb) {
        return (alpha & 0xFF) << 24 | rgb & 0xFFFFFF;
    }

    public static float[] RGBtoHSB(int color) {
        return java.awt.Color.RGBtoHSB(redI(color), greenI(color), blueI(color), null);
    }

    public static int blendColor(int colorA, int colorB, float ratio) {
        float inverse = 1.0f - ratio;
        return color((int) (alphaI(colorA) * inverse + alphaI(colorB) * ratio),
                (int) (redI(colorA) * inverse + redI(colorB) * ratio),
                (int) (greenI(colorA) * inverse + greenI(colorB) * ratio),
                (int) (blueI(colorA) * inverse + blueI(colorB) * ratio));
    }
}
