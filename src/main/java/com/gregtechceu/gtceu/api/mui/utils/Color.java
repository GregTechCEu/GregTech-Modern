package com.gregtechceu.gtceu.api.mui.utils;

import com.gregtechceu.gtceu.api.mui.base.drawable.IInterpolation;
import com.gregtechceu.gtceu.utils.serialization.json.JsonHelper;

import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.function.ToIntFunction;

/**
 * Utility class for dealing with colors.
 * <b>All methods assume the color int to be AARRGGBB if not stated otherwise!</b>
 * Most of the conversion methods are written by me with the help of <a
 * href=https://www.rapidtables.com/convert/color/>this website</a>.
 */
public class Color {

    /**
     * Creates a color int. All values should be 0 - 255
     */
    public static int rgb(int red, int green, int blue) {
        return argb(red, green, blue, 255);
    }

    /**
     * Creates a color int. All values should be 0 - 255
     */
    public static int argb(int red, int green, int blue, int alpha) {
        return ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | ((blue & 0xFF));
    }

    /**
     * Creates a color int. All values should be 0 - 1
     */
    public static int argb(float red, float green, float blue, float alpha) {
        return argb((int) (red * 255), (int) (green * 255), (int) (blue * 255), (int) (alpha * 255));
    }

    /**
     * Creates a color int. All values should be 0 - 255
     */
    public static int rgba(int red, int green, int blue, int alpha) {
        return ((red & 0xFF) << 24) | ((green & 0xFF) << 16) | ((blue & 0xFF) << 8) | (alpha & 0xFF);
    }

    /**
     * Creates a color int. All values should be 0 - 1
     */
    public static int rgba(float red, float green, float blue, float alpha) {
        return rgba((int) (red * 255), (int) (green * 255), (int) (blue * 255), (int) (alpha * 255));
    }

    /**
     * Creates a color int. All values should be 0 - 1
     */
    public static int rgb(float red, float green, float blue) {
        return argb(red, green, blue, 1f);
    }

    /**
     * Converts the HSV format into ARGB. With H being hue, S being saturation and V being value.
     *
     * @param hue        value from 0 to 360 (wraps around)
     * @param saturation value from 0 to 1
     * @param value      value from 0 to 1
     * @return the color with fully opacity
     */
    public static int ofHSV(float hue, float saturation, float value) {
        return ofHSV(hue, saturation, value, 1f);
    }

    /**
     * Converts the HSV format into ARGB. With H being hue, S being saturation and V being value.
     *
     * @param hue        value from 0 to 360 (wraps around)
     * @param saturation value from 0 to 1
     * @param value      value from 0 to 1
     * @param alpha      value from 0 to 1
     * @return the color
     */
    public static int ofHSV(float hue, float saturation, float value, float alpha) {
        hue %= 360;
        if (hue < 0) hue += 360;
        saturation = Mth.clamp(saturation, 0f, 1f);
        value = Mth.clamp(value, 0f, 1f);
        alpha = Mth.clamp(alpha, 0f, 1f);
        float c = value * saturation;
        float x = c * (1 - Math.abs(hue / 60f % 2 - 1));
        float m = value - c;
        return ofHxcm(hue, c, x, m, alpha);
    }

    /**
     * Converts the HSL format into ARGB. With H being hue, S being saturation and L being lightness.
     *
     * @param hue        value from 0 to 360 (wraps around)
     * @param saturation value from 0 to 1
     * @param lightness  value from 0 to 1
     * @return the color with fully opacity
     */
    public static int ofHSL(float hue, float saturation, float lightness) {
        return ofHSL(hue, saturation, lightness, 1f);
    }

    /**
     * Converts the HSL format into ARGB. With H being hue, S being saturation and L being lightness.
     *
     * @param hue        value from 0 to 360 (wraps around)
     * @param saturation value from 0 to 1
     * @param lightness  value from 0 to 1
     * @param alpha      value from 0 to 1
     * @return the color
     */
    public static int ofHSL(float hue, float saturation, float lightness, float alpha) {
        hue %= 360;
        if (hue < 0) hue += 360;
        saturation = Mth.clamp(saturation, 0f, 1f);
        lightness = Mth.clamp(lightness, 0f, 1f);
        alpha = Mth.clamp(alpha, 0f, 1f);
        float c = (1 - Math.abs(2 * lightness - 1)) * saturation;
        float x = c * (1 - Math.abs(hue / 60f % 2 - 1));
        float m = lightness - c / 2;
        return ofHxcm(hue, c, x, m, alpha);
    }

    /**
     * Helper method to calculate argb's of hue based formats.
     */
    private static int ofHxcm(float hue, float c, float x, float m, float alpha) {
        if (hue < 60) return argb(c + m, x + m, m, alpha);
        if (hue < 120) return argb(x + m, c + m, m, alpha);
        if (hue < 180) return argb(m, c + m, x + m, alpha);
        if (hue < 240) return argb(m, x + m, c + m, alpha);
        if (hue < 300) return argb(x + m, m, c + m, alpha);
        return argb(c + m, m, x + m, alpha);
    }

    /**
     * Converts the CMYK format into ARGB. All values are from 0 to 1.
     *
     * @param cyan    cyan value
     * @param magenta magenta value
     * @param yellow  yellow value
     * @param black   black value
     * @return ARGB color with fully opacity
     */
    public static int ofCMYK(float cyan, float magenta, float yellow, float black) {
        return ofCMYK(cyan, magenta, yellow, black, 1f);
    }

    /**
     * Converts the CMYK format into ARGB. All values are from 0 to 1.
     *
     * @param cyan    cyan value
     * @param magenta magenta value
     * @param yellow  yellow value
     * @param black   black value
     * @param alpha   alpha value
     * @return ARGB color
     */
    public static int ofCMYK(float cyan, float magenta, float yellow, float black, float alpha) {
        float oneMinusBlack = 1f - black;
        return argb((1 - cyan) * oneMinusBlack, (1 - magenta) * oneMinusBlack, (1 - yellow) * oneMinusBlack, alpha);
    }

    /**
     * Extracts the red bits from the ARGB color.
     *
     * @return the red value (from 0 to 255)
     */
    public static int getRed(int argb) {
        return argb >> 16 & 255;
    }

    /**
     * Extracts the green bits from the ARGB color.
     *
     * @return the green value (from 0 to 255)
     */
    public static int getGreen(int argb) {
        return argb >> 8 & 255;
    }

    /**
     * Extracts the blue bits from the ARGB color.
     *
     * @return the blue value (from 0 to 255)
     */
    public static int getBlue(int argb) {
        return argb & 255;
    }

    /**
     * Extracts the alpha bits from the ARGB color.
     *
     * @return the alpha value (from 0 to 255)
     */
    public static int getAlpha(int argb) {
        return argb >> 24 & 255;
    }

    /**
     * Extracts the red bits from the ARGB color.
     *
     * @return the red value (from 0 to 1)
     */
    public static float getRedF(int argb) {
        return getRed(argb) / 255f;
    }

    /**
     * Extracts the green bits from the ARGB color.
     *
     * @return the green value (from 0 to 1)
     */
    public static float getGreenF(int argb) {
        return getGreen(argb) / 255f;
    }

    /**
     * Extracts the blue bits from the ARGB color.
     *
     * @return the blue value (from 0 to 1)
     */
    public static float getBlueF(int argb) {
        return getBlue(argb) / 255f;
    }

    /**
     * Extracts the alpha bits from the ARGB color.
     *
     * @return the alpha value (from 0 to 1)
     */
    public static float getAlphaF(int argb) {
        return getAlpha(argb) / 255f;
    }

    /**
     * Replaces the red bits in the ARGB color.
     *
     * @param argb color in argb format
     * @param red  red value from 0 to 255
     * @return new ARGB color
     */
    public static int withRed(int argb, int red) {
        argb &= ~(0xFF << 16);
        return argb | red << 16;
    }

    /**
     * Replaces the green bits in the ARGB color.
     *
     * @param argb  color in argb format
     * @param green green value from 0 to 255
     * @return new ARGB color
     */
    public static int withGreen(int argb, int green) {
        argb &= ~(0xFF << 8);
        return argb | green << 8;
    }

    /**
     * Replaces the blue bits in the ARGB color.
     *
     * @param argb color in argb format
     * @param blue blue value from 0 to 255
     * @return new ARGB color
     */
    public static int withBlue(int argb, int blue) {
        argb &= ~0xFF;
        return argb | blue;
    }

    /**
     * Replaces the alpha bits in the ARGB color.
     *
     * @param argb  color in argb format
     * @param alpha alpha value from 0 to 255
     * @return new ARGB color
     */
    public static int withAlpha(int argb, int alpha) {
        argb &= ~(0xFF << 24);
        return argb | alpha << 24;
    }

    /**
     * Replaces the red bits in the ARGB color.
     *
     * @param argb color in argb format
     * @param red  red value from 0 to 1
     * @return new ARGB color
     */
    public static int withRed(int argb, float red) {
        return withRed(argb, (int) (red * 255));
    }

    /**
     * Replaces the green bits in the ARGB color.
     *
     * @param argb  color in argb format
     * @param green green value from 0 to 1
     * @return new ARGB color
     */
    public static int withGreen(int argb, float green) {
        return withGreen(argb, (int) (green * 255));
    }

    /**
     * Replaces the blue bits in the ARGB color.
     *
     * @param argb color in argb format
     * @param blue blue value from 0 to 1
     * @return new ARGB color
     */
    public static int withBlue(int argb, float blue) {
        return withBlue(argb, (int) (blue * 255));
    }

    /**
     * Replaces the alpha bits in the ARGB color.
     *
     * @param argb  color in argb format
     * @param alpha alpha value from 0 to 1
     * @return new ARGB color
     */
    public static int withAlpha(int argb, float alpha) {
        return withAlpha(argb, (int) (alpha * 255));
    }

    public static int ensureAlpha(int argb) {
        int a = getAlpha(argb);
        if (a == 0) argb = withAlpha(argb, 0xFF);
        return argb;
    }

    /**
     * Calculates the hue value (HSV or HSL format) from the ARGB color.
     *
     * @param argb color
     * @return hue value from 0 to 360
     */
    public static float getHue(int argb) {
        float r = getRedF(argb), g = getGreenF(argb), b = getBlueF(argb);
        if (r == g && r == b) return 0;
        float min = Math.min(r, Math.min(g, b));
        float hue = 0;
        if (r >= g && r >= b) {
            hue = ((g - b) / (r - min)) % 6;
        } else if (g >= r && g >= b) {
            hue = ((b - r) / (g - min)) + 2;
        } else if (b >= r && b >= g) {
            hue = ((r - g) / (b - min)) + 4;
        }
        hue *= 60;
        if (hue < 0) hue += 360;
        return hue;
    }

    /**
     * Calculates the HSV saturation value from the ARGB color.
     *
     * @param argb color
     * @return HSV saturation value from 0 to 1
     */
    public static float getHSVSaturation(int argb) {
        float r = getRedF(argb), g = getGreenF(argb), b = getBlueF(argb);
        float min = Math.min(r, Math.min(g, b));
        float max = Math.max(r, Math.max(g, b));
        return max == 0 ? 0 : (max - min) / max;
    }

    /**
     * Calculates the HSL saturation value from the ARGB color.
     *
     * @param argb color
     * @return HSL saturation value from 0 to 1
     */
    public static float getHSLSaturation(int argb) {
        float r = getRedF(argb), g = getGreenF(argb), b = getBlueF(argb);
        float min = Math.min(r, Math.min(g, b));
        float max = Math.max(r, Math.max(g, b));
        return (max - min) / (1 - Math.abs(max + min - 1));
    }

    /**
     * Calculates the HSV value from the ARGB color.
     *
     * @param argb color
     * @return HSV value from 0 to 1
     */
    public static float getValue(int argb) {
        float r = getRedF(argb), g = getGreenF(argb), b = getBlueF(argb);
        return Math.max(r, Math.max(g, b));
    }

    /**
     * Calculates the HSL lightness value from the ARGB color.
     *
     * @param argb color
     * @return HSL lightness value from 0 to 1
     */
    public static float getLightness(int argb) {
        float r = getRedF(argb), g = getGreenF(argb), b = getBlueF(argb);
        float min = Math.min(r, Math.min(g, b));
        float max = Math.max(r, Math.max(g, b));
        return (max + min) / 2;
    }

    /**
     * Replaces the hue value in the ARGB color in the HSV format.
     *
     * @param argb color
     * @param hue  new hue from 0 to 360
     * @return new ARGB color
     */
    public static int withHSVHue(int argb, float hue) {
        return ofHSV(hue, getHSVSaturation(argb), getValue(argb), getAlphaF(argb));
    }

    /**
     * Replaces the saturation value in the ARGB color in the HSV format.
     *
     * @param argb       color
     * @param saturation new saturation from 0 to 1
     * @return new ARGB color
     */
    public static int withHSVSaturation(int argb, float saturation) {
        return ofHSV(getHue(argb), saturation, getValue(argb), getAlphaF(argb));
    }

    /**
     * Replaces the value in the ARGB color in the HSV format.
     *
     * @param argb  color
     * @param value new value from 0 to 1
     * @return new ARGB color
     */
    public static int withValue(int argb, float value) {
        return ofHSV(getHue(argb), getHSVSaturation(argb), value, getAlphaF(argb));
    }

    /**
     * Replaces the hue value in the ARGB color in the HSL format.
     *
     * @param argb color
     * @param hue  new hue from 0 to 360
     * @return new ARGB color
     */
    public static int withHSLHue(int argb, float hue) {
        return ofHSL(hue, getHSLSaturation(argb), getLightness(argb), getAlphaF(argb));
    }

    /**
     * Replaces the saturation value in the ARGB color in the HSL format.
     *
     * @param argb       color
     * @param saturation new saturation from 0 to 1
     * @return new ARGB color
     */
    public static int withHSLSaturation(int argb, float saturation) {
        return ofHSL(getHue(argb), saturation, getLightness(argb), getAlphaF(argb));
    }

    /**
     * Replaces the lightness value in the ARGB color in the HSL format.
     *
     * @param argb      color
     * @param lightness new lightness from 0 to 1
     * @return new ARGB color
     */
    public static int withLightness(int argb, float lightness) {
        return ofHSL(getHue(argb), getHSLSaturation(argb), lightness, getAlphaF(argb));
    }

    /**
     * Calculates the CMYK cyan value from the ARGB color.
     *
     * @param argb color
     * @return cyan value from 0 to 1
     */
    public static float getCyan(int argb) {
        float r = getRedF(argb), g = getGreenF(argb), b = getBlueF(argb);
        float oneMinusBlack = Math.max(r, Math.max(g, b));
        return oneMinusBlack == 0 ? 0 : 1f - r / oneMinusBlack;
    }

    /**
     * Calculates the CMYK magenta value from the ARGB color.
     *
     * @param argb color
     * @return magenta value from 0 to 1
     */
    public static float getMagenta(int argb) {
        float r = getRedF(argb), g = getGreenF(argb), b = getBlueF(argb);
        float oneMinusBlack = Math.max(r, Math.max(g, b));
        return oneMinusBlack == 0 ? 0 : 1f - g / oneMinusBlack;
    }

    /**
     * Calculates the CMYK yellow value from the ARGB color.
     *
     * @param argb color
     * @return yellow value from 0 to 1
     */
    public static float getYellow(int argb) {
        float r = getRedF(argb), g = getGreenF(argb), b = getBlueF(argb);
        float oneMinusBlack = Math.max(r, Math.max(g, b));
        return oneMinusBlack == 0 ? 0 : 1f - b / oneMinusBlack;
    }

    /**
     * Calculates the CMYK black value from the ARGB color.
     *
     * @param argb color
     * @return black value from 0 to 1
     */
    public static float getBlack(int argb) {
        float r = getRedF(argb), g = getGreenF(argb), b = getBlueF(argb);
        return 1f - Math.max(r, Math.max(g, b));
    }

    /**
     * Replaces the cyan value in the ARGB color in the CMYK format.
     *
     * @param argb color
     * @param cyan new cyan from 0 to 1
     * @return new ARGB color
     */
    public static int withCyan(int argb, float cyan) {
        return ofCMYK(cyan, getMagenta(argb), getYellow(argb), getBlack(argb), getAlphaF(argb));
    }

    /**
     * Replaces the magenta value in the ARGB color in the CMYK format.
     *
     * @param argb    color
     * @param magenta new magenta from 0 to 1
     * @return new ARGB color
     */
    public static int withMagenta(int argb, float magenta) {
        return ofCMYK(getCyan(argb), magenta, getYellow(argb), getBlack(argb), getAlphaF(argb));
    }

    /**
     * Replaces the yellow value in the ARGB color in the CMYK format.
     *
     * @param argb   color
     * @param yellow new yellow from 0 to 1
     * @return new ARGB color
     */
    public static int withYellow(int argb, float yellow) {
        return ofCMYK(getCyan(argb), getMagenta(argb), yellow, getBlack(argb), getAlphaF(argb));
    }

    /**
     * Replaces the black value in the ARGB color in the CMYK format.
     *
     * @param argb  color
     * @param black new black from 0 to 1
     * @return new ARGB color
     */
    public static int withBlack(int argb, float black) {
        return ofCMYK(getCyan(argb), getMagenta(argb), getYellow(argb), black, getAlphaF(argb));
    }

    /**
     * Extracts the RGB bits into an array.
     *
     * @return rgba as an array [red, green, blue]
     */
    public static int[] getRGBValues(int argb) {
        return new int[] { getRed(argb), getGreen(argb), getBlue(argb) };
    }

    /**
     * Extracts the ARGB bits into an array.
     *
     * @return rgba as an array [red, green, blue, alpha]
     */
    public static int[] getARGBValues(int argb) {
        return new int[] { getRed(argb), getGreen(argb), getBlue(argb), getAlpha(argb) };
    }

    /**
     * Calculates the HSV values and puts them into an array.
     *
     * @param argb ARGB color
     * @return HSV values array
     */
    public static float[] getHSVValues(int argb) {
        return new float[] { getHue(argb), getHSVSaturation(argb), getValue(argb) };
    }

    /**
     * Calculates the HSL values and puts them into an array.
     *
     * @param argb ARGB color
     * @return HSL values array
     */
    public static float[] getHSLValues(int argb) {
        return new float[] { getHue(argb), getHSLSaturation(argb), getLightness(argb) };
    }

    /**
     * Calculates the CMYK values and puts them into an array.
     *
     * @param argb ARGB color
     * @return CMYK values array
     */
    public static float[] getCMYKValues(int argb) {
        return new float[] { getCyan(argb), getMagenta(argb), getYellow(argb), getBlack(argb) };
    }

    /**
     * Converts an RGBA int to an ARGB int.
     *
     * @param rgba RGBA color
     * @return ARGB color
     */
    public static int rgbaToArgb(int rgba) {
        return Color.argb(getAlpha(rgba), getRed(rgba), getGreen(rgba), getBlue(rgba));
    }

    /**
     * Converts an ARGB int to an RGBA int.
     *
     * @param argb ARGB color
     * @return RGBA color
     */
    public static int argbToRgba(int argb) {
        return Color.rgba(getRed(argb), getGreen(argb), getBlue(argb), getAlpha(argb));
    }

    /**
     * Inverts all color bytes except alpha.
     *
     * @param argb ARGB color
     * @return inverted color
     */
    public static int invert(int argb) {
        return Color.argb(255 - getRed(argb), 255 - getGreen(argb), 255 - getBlue(argb), getAlpha(argb));
    }

    /**
     * Multiplies each color byte with a factor to make it darker or brighter.
     *
     * @param argb          ARGB color
     * @param factor        multiplication factor
     * @param multiplyAlpha if alpha byte should be multiplied too
     * @return multiplied ARGB color
     */
    public static int multiply(int argb, float factor, boolean multiplyAlpha) {
        return argb(getRedF(argb) * factor, getGreenF(argb) * factor, getBlueF(argb) * factor,
                multiplyAlpha ? getAlphaF(argb) * factor : getAlphaF(argb));
    }

    /**
     * Calculates the average of each color byte in the array and puts it into a new ARGB color.
     *
     * @param colors ARGB colors
     * @return average ARGB color
     */
    public static int average(int... colors) {
        int r = 0, g = 0, b = 0, a = 0;
        for (int color : colors) {
            r += getRed(color);
            g += getGreen(color);
            b += getBlue(color);
            a += getAlpha(color);
        }
        return argb(r / colors.length, g / colors.length, b / colors.length, a / colors.length);
    }

    /**
     * Calculates the average of each color byte in the array and puts it into a new ARGB color.
     *
     * @param colorFunction function to extract a ARGB color from the objects
     * @param colorHolders  objects that can be converted to ARGB colors
     * @return average ARGB color
     */
    @SafeVarargs
    public static <T> int average(ToIntFunction<T> colorFunction, T... colorHolders) {
        int r = 0, g = 0, b = 0, a = 0;
        for (T colorHolder : colorHolders) {
            int color = colorFunction.applyAsInt(colorHolder);
            r += getRed(color);
            g += getGreen(color);
            b += getBlue(color);
            a += getAlpha(color);
        }
        return argb(r / colorHolders.length, g / colorHolders.length, b / colorHolders.length, a / colorHolders.length);
    }

    /**
     * Interpolates each color byte between two ARGB colors using linear interpolation and a progress value.
     *
     * @param color1 lower color
     * @param color2 higher color
     * @param value  progress value
     * @return interpolated ARGB color
     */
    public static int interpolate(int color1, int color2, float value) {
        return interpolate(Interpolation.LINEAR, color1, color2, value);
    }

    /**
     * Interpolates each color byte between two ARGB colors with an interpolation curve and a progress value.
     *
     * @param curve  interpolation curve
     * @param color1 lower color
     * @param color2 higher color
     * @param value  progress value
     * @return interpolated ARGB color
     */
    public static int interpolate(IInterpolation curve, int color1, int color2, float value) {
        value = Mth.clamp(value, 0, 1);
        int r = (int) curve.interpolate(Color.getRed(color1), Color.getRed(color2), value);
        int g = (int) curve.interpolate(Color.getGreen(color1), Color.getGreen(color2), value);
        int b = (int) curve.interpolate(Color.getBlue(color1), Color.getBlue(color2), value);
        int a = (int) curve.interpolate(Color.getAlpha(color1), Color.getAlpha(color2), value);
        return Color.argb(r, g, b, a);
    }

    /**
     * A helper method to apply a color to rendering.
     * If the alpha is 0 and any other value is not null, the alpha will be set to max.
     *
     * @param color argb color
     */
    @OnlyIn(Dist.CLIENT)
    public static void setGlColor(int color) {
        if (color == 0) {
            RenderSystem.setShaderColor(0, 0, 0, 0);
            return;
        }
        float a = getAlphaF(color);
        if (a == 0) a = 1f;
        RenderSystem.setShaderColor(getRedF(color), getGreenF(color), getBlueF(color), a);
    }

    /**
     * Applies a ARGB color to OpenGL with a fixed alpha value of 255.
     *
     * @param color ARGB color.
     */
    @OnlyIn(Dist.CLIENT)
    public static void setGlColorOpaque(int color) {
        if (color == 0) {
            RenderSystem.setShaderColor(0, 0, 0, 0);
            return;
        }
        RenderSystem.setShaderColor(getRedF(color), getGreenF(color), getBlueF(color), 1f);
    }

    /**
     * Enables the OpenGL color mask and sets its colors to white.
     */
    @OnlyIn(Dist.CLIENT)
    public static void resetGlColor() {
        RenderSystem.colorMask(true, true, true, true);
        setGlColorOpaque(WHITE.main);
    }

    /**
     * Parses a ARGB color of a json element.
     *
     * @param jsonElement json element
     * @return ARGB color
     * @throws JsonParseException if color could not be parsed
     */
    public static int ofJson(JsonElement jsonElement) {
        if (jsonElement.isJsonPrimitive()) {
            int color = (int) (long) Long.decode(jsonElement.getAsString()); // bruh
            if (color != 0 && getAlpha(color) == 0) {
                return withAlpha(color, 255);
            }
            return color;
        }
        if (jsonElement.isJsonObject()) {
            JsonObject json = jsonElement.getAsJsonObject();
            String alphaS = JsonHelper.getString(json, "1f", "a", "alpha");
            float alphaF;
            int alpha;
            if (alphaS.contains(".") || alphaS.endsWith("f") || alphaS.endsWith("F") || alphaS.endsWith("d") ||
                    alphaS.endsWith("D")) {
                try {
                    alphaF = Mth.clamp(Float.parseFloat(alphaS), 0f, 1f);
                    alpha = (int) (alphaF * 255);
                } catch (NumberFormatException e) {
                    throw new JsonParseException("Failed to parse alpha value", e);
                }
            } else {
                try {
                    alpha = Mth.clamp(Integer.parseInt(alphaS), 0, 255);
                    alphaF = alpha / 255f;
                } catch (NumberFormatException e) {
                    throw new JsonParseException("Failed to parse alpha value", e);
                }
            }
            if (hasRGB(json)) {
                if (hasHS(json) || hasV(json) || hasL(json))
                    throw new JsonParseException("Found RGB values, but also HSV or HSL values!");
                if (hasCMYK(json))
                    throw new JsonParseException("Found RGB values, but also CMYK values!");
                int red = JsonHelper.getInt(json, 255, "r", "red");
                int green = JsonHelper.getInt(json, 255, "g", "green");
                int blue = JsonHelper.getInt(json, 255, "b", "blue");
                if ((red | green | blue) != 0 && alpha == 0) {
                    alpha = 255;
                }
                return Color.argb(red, green, blue, alpha);
            }
            if (hasHS(json)) {
                if (hasCMYK(json))
                    throw new JsonParseException("Found HSV or HSL values, but also CMYK values!");
                int hue = JsonHelper.getInt(json, 0, "h", "hue");
                float saturation = JsonHelper.getFloat(json, 0, "s", "saturation");
                if (hasV(json)) {
                    if (hasL(json)) throw new JsonParseException("Found HSV values, but also HSL values!");
                    float value = JsonHelper.getFloat(json, 1f, "v", "value");
                    return ofHSV(hue, saturation, value, alphaF);
                }
                float lightness = JsonHelper.getFloat(json, 0.5f, "l", "lightness");
                return ofHSL(hue, saturation, lightness, alphaF);
            }
            if (hasCMYK(json)) {
                float c = JsonHelper.getFloat(json, 1f, "c", "cyan");
                float m = JsonHelper.getFloat(json, 1f, "m", "magenta");
                float y = JsonHelper.getFloat(json, 1f, "y", "yellow");
                float k = JsonHelper.getFloat(json, 1f, "k", "black");
                return ofCMYK(c, m, y, k, alphaF);
            }
            throw new JsonParseException("Empty color declaration");
        }
        throw new JsonParseException("Color must be a primitive or an object!");
    }

    private static boolean hasRGB(JsonObject json) {
        return json.has("r") || json.has("red") ||
                json.has("g") || json.has("green") ||
                json.has("b") || json.has("blue");
    }

    private static boolean hasHS(JsonObject json) {
        return json.has("h") || json.has("hue") ||
                json.has("s") || json.has("saturation");
    }

    private static boolean hasV(JsonObject json) {
        return json.has("v") || json.has("value");
    }

    private static boolean hasL(JsonObject json) {
        return json.has("l") || json.has("lightness");
    }

    private static boolean hasCMYK(JsonObject json) {
        return json.has("c") || json.has("cyan") ||
                json.has("m") || json.has("magenta") ||
                json.has("y") || json.has("yellow") ||
                json.has("k") || json.has("black");
    }

    public static final ColorShade WHITE = ColorShade.builder(0xFFFFFFFF)
            .addDarker(0xFFF7F7F7, 0xFFEFEFEF, 0xFFE7E7E7, 0xFFDFDFDF, 0xFFD7D7D7, 0xFFCFCFCF, 0xFFC7C7C7, 0xFFBFBFBF)
            .build();

    public static final ColorShade BLACK = ColorShade.builder(0xFF000000)
            .addBrighter(0xFF080808, 0xFF101010, 0xFF181818, 0xFF202020, 0xFF282828, 0xFF303030, 0xFF383838, 0xFF404040)
            .build();

    public static final ColorShade RED = ColorShade.builder(0xFFF44336)
            .addBrighter(0xFFEF5350, 0xFFE57373, 0xFFEF9A9A, 0xFFFFCDD2, 0xFFFFEBEE)
            .addDarker(0xFFE53935, 0xFFD32F2F, 0xFFC62828, 0xFFB71C1C)
            .build();

    public static final ColorShade RED_ACCENT = ColorShade.builder(0xFFFF5252)
            .addBrighter(0xFFFF8A80)
            .addDarker(0xFFFF1744, 0xFFD50000)
            .build();

    public static final ColorShade PINK = ColorShade.builder(0xFFE91E63)
            .addBrighter(0xFFEC407A, 0xFFF06292, 0xFFF48FB1, 0xFFF8BBD0, 0xFFFCE4EC)
            .addDarker(0xFFD81B60, 0xFFC2185B, 0xFFAD1457, 0xFF880E4F)
            .build();

    public static final ColorShade PINK_ACCENT = ColorShade.builder(0xFFFF4081)
            .addBrighter(0xFFFF80AB)
            .addDarker(0xFFF50057, 0xFFC51162)
            .build();

    public static final ColorShade PURPLE = ColorShade.builder(0xFF9C27B0)
            .addBrighter(0xFFAB47BC, 0xFFBA68C8, 0xFFCE93D8, 0xFFE1BEE7, 0xFFF3E5F5)
            .addDarker(0xFF8E24AA, 0xFF7B1FA2, 0xFF6A1B9A, 0xFF4A148C)
            .build();

    public static final ColorShade PURPLE_ACCENT = ColorShade.builder(0xFFE040FB)
            .addBrighter(0xFFEA80FC)
            .addDarker(0xFFD500F9, 0xFFAA00FF)
            .build();

    public static final ColorShade DEEP_PURPLE = ColorShade.builder(0xFF673AB7)
            .addBrighter(0xFF7E57C2, 0xFF9575CD, 0xFFB39DDB, 0xFFD1C4E9, 0xFFEDE7F6)
            .addDarker(0xFF5E35B1, 0xFF512DA8, 0xFF4527A0, 0xFF311B92)
            .build();

    public static final ColorShade DEEP_PURPLE_ACCENT = ColorShade.builder(0xFF7C4DFF)
            .addBrighter(0xFFB388FF)
            .addDarker(0xFF651FFF, 0xFF651FFF)
            .build();

    public static final ColorShade INDIGO = ColorShade.builder(0xFF3F51B5)
            .addBrighter(0xFF5C6BC0, 0xFF7986CB, 0xFF9FA8DA, 0xFFC5CAE9, 0xFFE8EAF6)
            .addDarker(0xFF3949AB, 0xFF303F9F, 0xFF283593, 0xFF1A237E)
            .build();

    public static final ColorShade INDIGO_ACCENT = ColorShade.builder(0xFF536DFE)
            .addBrighter(0xFF8C9EFF)
            .addDarker(0xFF3D5AFE, 0xFF304FFE)
            .build();

    public static final ColorShade BLUE = ColorShade.builder(0xFF2196F3)
            .addBrighter(0xFF42A5F5, 0xFF64B5F6, 0xFF90CAF9, 0xFFBBDEFB, 0xFFE3F2FD)
            .addDarker(0xFF1E88E5, 0xFF1976D2, 0xFF1565C0, 0xFF0D47A1)
            .build();

    public static final ColorShade BLUE_ACCENT = ColorShade.builder(0xFF448AFF)
            .addBrighter(0xFF82B1FF)
            .addDarker(0xFF2979FF, 0xFF2962FF)
            .build();

    public static final ColorShade LIGHT_BLUE = ColorShade.builder(0xFF03A9F4)
            .addBrighter(0xFF29B6F6, 0xFF4FC3F7, 0xFF81D4FA, 0xFFB3E5FC, 0xFFE1F5FE)
            .addDarker(0xFF039BE5, 0xFF0288D1, 0xFF0277BD, 0xFF01579B)
            .build();

    public static final ColorShade LIGHT_BLUE_ACCENT = ColorShade.builder(0xFF40C4FF)
            .addBrighter(0xFF80D8FF)
            .addDarker(0xFF00B0FF, 0xFF0091EA)
            .build();

    public static final ColorShade CYAN = ColorShade.builder(0xFF00BCD4)
            .addBrighter(0xFF26C6DA, 0xFF4DD0E1, 0xFF80DEEA, 0xFFB2EBF2, 0xFFE0F7FA)
            .addDarker(0xFF00ACC1, 0xFF0097A7, 0xFF00838F, 0xFF006064)
            .build();

    public static final ColorShade CYAN_ACCENT = ColorShade.builder(0xFF18FFFF)
            .addBrighter(0xFF84FFFF)
            .addDarker(0xFF00E5FF, 0xFF00B8D4)
            .build();

    public static final ColorShade TEAL = ColorShade.builder(0xFF009688)
            .addBrighter(0xFF26A69A, 0xFF4DB6AC, 0xFF80CBC4, 0xFFB2DFDB, 0xFFE0F2F1)
            .addDarker(0xFF00897B, 0xFF00796B, 0xFF00695C, 0xFF004D40)
            .build();

    public static final ColorShade TEAL_ACCENT = ColorShade.builder(0xFF64FFDA)
            .addBrighter(0xFFA7FFEB)
            .addDarker(0xFF1DE9B6, 0xFF00BFA5)
            .build();

    public static final ColorShade GREEN = ColorShade.builder(0xFF4CAF50)
            .addBrighter(0xFF66BB6A, 0xFF81C784, 0xFFA5D6A7, 0xFFC8E6C9, 0xFFE8F5E9)
            .addDarker(0xFF43A047, 0xFF388E3C, 0xFF2E7D32, 0xFF1B5E20)
            .build();

    public static final ColorShade GREEN_ACCENT = ColorShade.builder(0xFF69F0AE)
            .addBrighter(0xFFB9F6CA)
            .addDarker(0xFF00E676, 0xFF00C853)
            .build();

    public static final ColorShade LIGHT_GREEN = ColorShade.builder(0xFF8BC34A)
            .addBrighter(0xFF9CCC65, 0xFFAED581, 0xFFC5E1A5, 0xFFDCEDC8, 0xFFF1F8E9)
            .addDarker(0xFF7CB342, 0xFF689F38, 0xFF558B2F, 0xFF33691E)
            .build();

    public static final ColorShade LIGHT_GREEN_ACCENT = ColorShade.builder(0xFFB2FF59)
            .addBrighter(0xFFCCFF90)
            .addDarker(0xFF76FF03, 0xFF64DD17)
            .build();

    public static final ColorShade LIME = ColorShade.builder(0xFFCDDC39)
            .addBrighter(0xFFD4E157, 0xFFDCE775, 0xFFE6EE9C, 0xFFF0F4C3, 0xFFF9FBE7)
            .addDarker(0xFFC0CA33, 0xFFAFB42B, 0xFF9E9D24, 0xFF827717)
            .build();

    public static final ColorShade LIME_ACCENT = ColorShade.builder(0xFFEEFF41)
            .addBrighter(0xFFF4FF81)
            .addDarker(0xFFC6FF00, 0xFFAEEA00)
            .build();

    public static final ColorShade YELLOW = ColorShade.builder(0xFFFFEB3B)
            .addBrighter(0xFFFFEE58, 0xFFFFF176, 0xFFFFF59D, 0xFFFFF9C4, 0xFFFFFDE7)
            .addDarker(0xFFFDD835, 0xFFFBC02D, 0xFFF9A825, 0xFFF57F17)
            .build();

    public static final ColorShade YELLOW_ACCENT = ColorShade.builder(0xFFFFFF00)
            .addBrighter(0xFFFFFF8D)
            .addDarker(0xFFFFEA00, 0xFFFFD600)
            .build();

    public static final ColorShade AMBER = ColorShade.builder(0xFFFFC107)
            .addBrighter(0xFFFFCA28, 0xFFFFD54F, 0xFFFFE082, 0xFFFFECB3, 0xFFFFF8E1)
            .addDarker(0xFFFFB300, 0xFFFFA000, 0xFFFF8F00, 0xFFFF6F00)
            .build();

    public static final ColorShade AMBER_ACCENT = ColorShade.builder(0xFFFFD740)
            .addBrighter(0xFFFFE57F)
            .addDarker(0xFFFFC400, 0xFFFFAB00)
            .build();

    public static final ColorShade ORANGE = ColorShade.builder(0xFFFF9800)
            .addBrighter(0xFFFFA726, 0xFFFFB74D, 0xFFFFCC80, 0xFFFFE0B2, 0xFFFFF3E0)
            .addDarker(0xFFFB8C00, 0xFFF57C00, 0xFFEF6C00, 0xFFE65100)
            .build();

    public static final ColorShade ORANGE_ACCENT = ColorShade.builder(0xFFFFAB40)
            .addBrighter(0xFFFFD180)
            .addDarker(0xFFFF9100, 0xFFFF6D00)
            .build();

    public static final ColorShade DEEP_ORANGE = ColorShade.builder(0xFFFF5722)
            .addBrighter(0xFFFF7043, 0xFFFF8A65, 0xFFFFAB91, 0xFFFFCCBC, 0xFFFBE9E7)
            .addDarker(0xFFF4511E, 0xFFE64A19, 0xFFD84315, 0xFFBF360C)
            .build();

    public static final ColorShade DEEP_ORANGE_ACCENT = ColorShade.builder(0xFFFF6E40)
            .addBrighter(0xFFFF9E80)
            .addDarker(0xFFFF3D00, 0xFFDD2C00)
            .build();

    public static final ColorShade BROWN = ColorShade.builder(0xFF795548)
            .addBrighter(0xFF8D6E63, 0xFFA1887F, 0xFFBCAAA4, 0xFFD7CCC8, 0xFFEFEBE9)
            .addDarker(0xFF6D4C41, 0xFF5D4037, 0xFF4E342E, 0xFF3E2723)
            .build();

    public static final ColorShade GREY = ColorShade.builder(0xFF9E9E9E)
            .addBrighter(0xFFBDBDBD, 0xFFE0E0E0, 0xFFEEEEEE, 0xFFF5F5F5, 0xFFFAFAFA)
            .addDarker(0xFF757575, 0xFF616161, 0xFF424242, 0xFF212121)
            .build();

    public static final ColorShade BLUE_GREY = ColorShade.builder(0xFF607D8B)
            .addBrighter(0xFF78909C, 0xFF90A4AE, 0xFFB0BEC5, 0xFFCFD8DC, 0xFFECEFF1)
            .addDarker(0xFF546E7A, 0xFF455A64, 0xFF37474F, 0xFF263238)
            .build();
}
