package com.gregtechceu.gtceu.client.util;

public class DrawUtil {

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
