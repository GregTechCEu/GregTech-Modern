package net.minecraft.util;

public final class FastColor {

    private FastColor() {}

    public static final class ARGB32 {

        private ARGB32() {}

        public static int alpha(int color) {
            return ARGB.alpha(color);
        }

        public static int red(int color) {
            return ARGB.red(color);
        }

        public static int green(int color) {
            return ARGB.green(color);
        }

        public static int blue(int color) {
            return ARGB.blue(color);
        }

        public static int color(int alpha, int red, int green, int blue) {
            return ARGB.color(alpha, red, green, blue);
        }

        public static int color(int alpha, int rgb) {
            return ARGB.color(alpha, rgb);
        }
    }
}
