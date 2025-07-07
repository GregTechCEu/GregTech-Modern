package com.gregtechceu.gtceu.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.DyeColor;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class GradientUtil {

    private GradientUtil() {}

    public static final Function<DyeColor, ChatFormatting> DYE_COLOR_TO_FORMATTING = Util.make(() -> {
        Object2ObjectMap<DyeColor, ChatFormatting> map = new Object2ObjectOpenHashMap<>();
        map.put(DyeColor.WHITE, ChatFormatting.WHITE);
        map.put(DyeColor.ORANGE, ChatFormatting.GOLD);
        map.put(DyeColor.MAGENTA, ChatFormatting.DARK_PURPLE);
        map.put(DyeColor.LIGHT_BLUE, ChatFormatting.BLUE);
        map.put(DyeColor.YELLOW, ChatFormatting.YELLOW);
        map.put(DyeColor.LIME, ChatFormatting.GREEN);
        map.put(DyeColor.PINK, ChatFormatting.LIGHT_PURPLE);
        map.put(DyeColor.GRAY, ChatFormatting.DARK_GRAY);
        map.put(DyeColor.LIGHT_GRAY, ChatFormatting.GRAY);
        map.put(DyeColor.CYAN, ChatFormatting.DARK_AQUA);
        map.put(DyeColor.PURPLE, ChatFormatting.LIGHT_PURPLE);
        map.put(DyeColor.BLUE, ChatFormatting.DARK_BLUE);
        map.put(DyeColor.BROWN, ChatFormatting.GOLD);
        map.put(DyeColor.GREEN, ChatFormatting.DARK_GREEN);
        map.put(DyeColor.RED, ChatFormatting.RED);
        map.put(DyeColor.BLACK, ChatFormatting.BLACK);
        return map;
    });

    public static int convertRGBtoARGB(int colorValue) {
        return convertRGBtoARGB(colorValue, 0xFF);
    }

    public static int convertRGBtoARGB(int colorValue, int opacity) {
        // preserve existing opacity if present
        if (((colorValue >> 24) & 0xFF) != 0) return colorValue;
        return opacity << 24 | colorValue;
    }

    public static int convertARGBToABGR(int argb) {
        int r = (argb >> 16) & 0xFF;
        int b = argb & 0xFF;
        return (argb & 0xFF00FF00) | (b << 16) | r;
    }

    public static int convertARGBToRGBA(int argb) {
        return argb << 8 | (argb >>> 24);
    }

    public static float[] getRGB(int color) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        // noinspection PointlessBitwiseExpression
        float b = ((color >> 0) & 0xFF) / 255f;
        return new float[] { r, g, b };
    }

    public static int multiplyBlendWithAlpha(int c1, int c2) {
        int x1 = (c1 & 0xff);
        int y1 = ((c1 & 0xff00) >> 8);
        int z1 = ((c1 & 0xff0000) >> 16);
        int w1 = ((c1 & 0xff000000) >> 24);

        int x2 = (c2 & 0xff);
        int y2 = ((c2 & 0xff00) >> 8);
        int z2 = ((c2 & 0xff0000) >> 16);
        int w2 = ((c2 & 0xff000000) >> 24);

        int x = (x1 * x2) / 255;
        int y = (y1 * y2) / 255;
        int z = (z1 * z2) / 255;
        int w = (w1 * w2) / 255;
        // harcode an exception for max alpha because it ends up as 0 from the above calculations
        if (w1 == -1 && w2 == -1) {
            w = 0xff;
        }

        return w << 24 | z << 16 | y << 8 | x;
    }

    public static int blend(int c1, int c2, float ratio) {
        if (ratio > 1f) ratio = 1f;
        else if (ratio < 0f) ratio = 0f;
        float iRatio = 1.0f - ratio;

        int a1 = (c1 >> 24 & 0xff);
        int r1 = ((c1 & 0xff0000) >> 16);
        int g1 = ((c1 & 0xff00) >> 8);
        int b1 = (c1 & 0xff);

        int a2 = (c2 >> 24 & 0xff);
        int r2 = ((c2 & 0xff0000) >> 16);
        int g2 = ((c2 & 0xff00) >> 8);
        int b2 = (c2 & 0xff);

        int a = (int) ((a1 * iRatio) + (a2 * ratio));
        int r = (int) ((r1 * iRatio) + (r2 * ratio));
        int g = (int) ((g1 * iRatio) + (g2 * ratio));
        int b = (int) ((b1 * iRatio) + (b2 * ratio));

        return a << 24 | r << 16 | g << 8 | b;
    }

    public static IntIntPair getGradient(int rgb, int luminanceDifference) {
        float[] hsl = convertRGBtoHSL(rgb);
        float[] upshade = new float[3];
        float[] downshade = new float[3];
        System.arraycopy(hsl, 0, upshade, 0, 3);
        System.arraycopy(hsl, 0, downshade, 0, 3);
        upshade[2] = upshade[2] + luminanceDifference;
        if (upshade[2] > 100.0F) upshade[2] = 100.0F;
        downshade[2] = downshade[2] - luminanceDifference;
        if (downshade[2] < 0.0F) downshade[2] = 0.0F;
        int upshadeRgb = convertHSLToRGB(upshade);
        int downshadeRgb = convertHSLToRGB(downshade);
        return IntIntPair.of(downshadeRgb, upshadeRgb);
    }

    public static float[] convertRGBtoHSL(int rgbColor) {
        // Get RGB values in the range 0 - 1
        float r = ((rgbColor >> 16) & 0xFF) / 255f;
        float g = ((rgbColor >> 8) & 0xFF) / 255f;
        // noinspection PointlessBitwiseExpression
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

        return new float[] { h, s * 100, l * 100 };
    }

    public static int convertHSLToRGB(float[] hsl) {
        return convertHSLToRGB(hsl[0], hsl[1], hsl[2]);
    }

    public static int convertHSLToRGB(float h, float s, float l) {
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

        int r = (int) (Math.max(0, hueToRGB(p, q, h + (1.0F / 3.0F))) * 255);
        int g = (int) (Math.max(0, hueToRGB(p, q, h)) * 255);
        int b = (int) (Math.max(0, hueToRGB(p, q, h - (1.0F / 3.0F))) * 255);

        return FastColor.ARGB32.color(255, r, g, b);
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
            return q;
        }
        if (3 * h < 2) {
            return p + ((q - p) * 6 * ((2.0F / 3.0F) - h));
        }
        return p;
    }

    /**
     * Determines the dye color with the nearest text color to the specified RGB value
     */
    public static DyeColor determineDyeColorByTextColor(int textColor) {
        return determineEnumColor(textColor, DyeColor.class, DyeColor::getTextColor);
    }

    /**
     * Determines the dye color with the nearest map color to the specified RGB value
     */
    public static DyeColor determineDyeColorByMapColor(int mapColor) {
        return determineEnumColor(mapColor, DyeColor.class, dye -> dye.getMapColor().col);
    }

    /**
     * Determines the text (color) format string with the nearest color to the specified RGB value
     */
    @SuppressWarnings("DataFlowIssue")
    public static ChatFormatting determineFormatByTextColor(int rgbColor) {
        return determineEnumColor(rgbColor, ChatFormatting.class, ChatFormatting::isColor, ChatFormatting::getColor);
    }

    /**
     * Determines the nearest <strong>dye color</strong> to the specified map color
     * and finds the matching text color format.
     */
    public static ChatFormatting determineFormatByMapColor(int mapColor) {
        DyeColor dye = determineDyeColorByMapColor(mapColor);
        return DYE_COLOR_TO_FORMATTING.apply(dye);
    }

    public static <T extends Enum<T>> T determineEnumColor(int rgbColor, Class<T> clazz, ToIntFunction<T> colorGetter) {
        return determineEnumColor(rgbColor, clazz, c -> true, colorGetter);
    }

    public static <T extends Enum<T>> T determineEnumColor(int rgbColor, Class<T> clazz,
                                                           Predicate<T> validColor, ToIntFunction<T> colorGetter) {
        float[] rgb = getRGB(rgbColor);

        double min = Double.MAX_VALUE;
        T minColor = null;
        for (T color : clazz.getEnumConstants()) {
            if (!validColor.test(color)) continue;

            float[] dyeRGB = getRGB(colorGetter.applyAsInt(color));

            float rDist = (rgb[0] - dyeRGB[0]);
            float bDist = (rgb[1] - dyeRGB[1]);
            float gDist = (rgb[2] - dyeRGB[2]);

            double squareDistance = rDist * rDist + bDist * bDist + gDist * gDist;

            if (Double.compare(min, squareDistance) < 0) {
                minColor = color;
                min = squareDistance;
            }
        }
        return minColor;
    }

    // Deprecated functions

    /**
     * @apiNote use {@link ##convertARGBToABGR(int)} instead
     */
    @ApiStatus.Obsolete(since = "7.0.0")
    public static int argbToAbgr(int argb) {
        return convertARGBToABGR(argb);
    }

    /**
     * @apiNote use {@link #convertARGBToRGBA(int)} instead
     */
    @ApiStatus.Obsolete(since = "7.0.0")
    public static int argbToRgba(int argb) {
        return convertARGBToRGBA(argb);
    }

    /**
     * @apiNote use {@link #convertHSLToRGB(float[])} instead
     */
    @ApiStatus.Obsolete(since = "7.0.0")
    public static int toRGB(float[] hsv) {
        return convertHSLToRGB(hsv);
    }

    /**
     * @apiNote use {@link #convertHSLToRGB(float, float, float)} instead
     */
    @ApiStatus.Obsolete(since = "7.0.0")
    public static int toRGB(float h, float s, float l) {
        return convertHSLToRGB(h, s, l);
    }

    /**
     * @apiNote use {@link #convertRGBtoHSL(int)} instead
     */
    @ApiStatus.Obsolete(since = "7.0.0")
    public static float[] RGBtoHSL(int rgbColor) {
        return convertRGBtoHSL(rgbColor);
    }
}
