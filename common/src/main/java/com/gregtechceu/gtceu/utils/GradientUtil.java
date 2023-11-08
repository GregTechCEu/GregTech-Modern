package com.gregtechceu.gtceu.utils;


import net.minecraft.util.Tuple;

public class GradientUtil {

    private GradientUtil() {
    }

    public static int argbToAbgr(int argb) {
        int r = (argb >> 16) & 0xFF;
        int b = argb & 0xFF;
        return (argb & 0xFF00FF00) | (b << 16) | r;
    }

    public static int argbToRgba(int argb) {
         return argb << 8 | (argb >>> 24);
    }

    public static float[] getRGB(int color) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        //noinspection PointlessBitwiseExpression
        float b = ((color >> 0) & 0xFF) / 255f;
        return new float[]{r, g, b};
    }

    public static int multiplyBlendRGBA(int c1, int c2) {
        int a1 = (c1 & 0xff);
        int r1 = ((c1 & 0xff000000) >> 24);
        int g1 = ((c1 & 0xff0000) >> 16);
        int b1 = ((c1  & 0xff00) >> 8);

        int a2 = (c2 & 0xff);
        int r2 = ((c2 & 0xff000000) >> 24);
        int g2 = ((c2 & 0xff0000) >> 16);
        int b2 = ((c2  & 0xff00) >> 8);

        int a = (a1 * a2) / 255;
        int r = (r1 * r2) / 255;
        int g = (g1 * g2) / 255;
        int b = (b1 * b2) / 255;

        return a << 24 | r << 16 | g << 8 | b;
    }

    public static int blend(int c1, int c2, float ratio) {
        if ( ratio > 1f ) ratio = 1f;
        else if ( ratio < 0f ) ratio = 0f;
        float iRatio = 1.0f - ratio;

        int a1 = (c1 >> 24 & 0xff);
        int r1 = ((c1 & 0xff0000) >> 16);
        int g1 = ((c1 & 0xff00) >> 8);
        int b1 = (c1 & 0xff);

        int a2 = (c2 >> 24 & 0xff);
        int r2 = ((c2 & 0xff0000) >> 16);
        int g2 = ((c2 & 0xff00) >> 8);
        int b2 = (c2 & 0xff);

        int a = (int)((a1 * iRatio) + (a2 * ratio));
        int r = (int)((r1 * iRatio) + (r2 * ratio));
        int g = (int)((g1 * iRatio) + (g2 * ratio));
        int b = (int)((b1 * iRatio) + (b2 * ratio));

        return a << 24 | r << 16 | g << 8 | b;
    }

    public static Tuple<float[], float[]> getGradient(int rgb, int luminanceDifference) {
        float[] hsl = RGBtoHSL(rgb);
        float[] upshade = new float[3];
        float[] downshade = new float[3];
        System.arraycopy(hsl, 0, upshade, 0, 3);
        System.arraycopy(hsl, 0, downshade, 0, 3);
        upshade[2] = upshade[2] + luminanceDifference;
        if (upshade[2] > 100.0F) upshade[2] = 100.0F;
        downshade[2] = downshade[2] - luminanceDifference;
        if (downshade[2] < 0.0F) downshade[2] = 0.0F;
        float[] upshadeRgb = toRGB(upshade);
        float[] downshadeRgb = toRGB(downshade);
        return new Tuple<>(downshadeRgb, upshadeRgb);
    }

    public static float[] RGBtoHSL(int rgbColor) {
        // Get RGB values in the range 0 - 1
        float r = ((rgbColor >> 16) & 0xFF) / 255f;
        float g = ((rgbColor >> 8) & 0xFF) / 255f;
        //noinspection PointlessBitwiseExpression
        float b = ((rgbColor >> 0) & 0xFF) / 255f;

        // Minimum and Maximum RGB values are used in the HSL calculations
        float min = Math.min(r, Math.min(g, b));
        float max = Math.max(r, Math.max(g, b));

        // Calculate the Hue
        float h = 0;
        if (max == min) {
            h = 0;
        } else if (max == r) {
            h = ((60 * (g - b) / (max - min)) + 360) % 360;
        } else if (max == g) {
            h = (60 * (b - r) / (max - min)) + 120;
        } else if (max == b) {
            h = (60 * (r - g) / (max - min)) + 240;
        }

        // Calculate the Luminance
        float l = (max + min) / 2;

        // Calculate the Saturation
        float s;
        if (max == min) {
            s = 0;
        } else if (l <= 0.5F) {
            s = (max - min) / (max + min);
        } else {
            s = (max - min) / (2 - max - min);
        }

        return new float[] {h, s * 100, l * 100};
    }

    public static float[] toRGB(float[] hsv) {
        return toRGB(hsv[0], hsv[1], hsv[2]);
    }

    public static float[] toRGB(float h, float s, float l) {
        // Formula needs all values between 0 - 1
        h = h % 360.0F;
        h /= 360.0F;
        s /= 100.0F;
        l /= 100.0F;

        float q;
        if (l < 0.5F) {
            q = l * (1 + s);
        } else {
            q = (l + s) - (s * l);
        }

        float p = 2 * l - q;

        float r = Math.max(0, hueToRGB(p, q, h + (1.0F / 3.0F)));
        float g = Math.max(0, hueToRGB(p, q, h));
        float b = Math.max(0, hueToRGB(p, q, h - (1.0F / 3.0F)));

        r = Math.min(r, 1.0F);
        g = Math.min(g, 1.0F);
        b = Math.min(b, 1.0F);

        return new float[]{r, g, b};
    }

    private static float hueToRGB(float p, float q, float h) {
        if (h < 0) {
            h += 1;
        }
        if (h > 1) {
            h -= 1;
        }
        if (6 * h < 1) {
            return p + ((q - p) * 6 * h);
        }
        if (2 * h < 1) {
            return  q;
        }
        if (3 * h < 2) {
            return p + ( (q - p) * 6 * ((2.0F / 3.0F) - h) );
        }
        return p;
    }
}
