package com.lowdragmc.lowdraglib.gui.util;

public class TextFormattingUtil {

    public static String formatLongToCompactString(long value, int precision) {
        return compact(value, precision);
    }

    public static String formatLongToCompactStringBuckets(long value, int precision) {
        return compact(value, precision);
    }

    private static String compact(long value, int precision) {
        double number = value;
        String suffix = "";
        if (Math.abs(number) >= 1_000_000_000) {
            number /= 1_000_000_000;
            suffix = "G";
        } else if (Math.abs(number) >= 1_000_000) {
            number /= 1_000_000;
            suffix = "M";
        } else if (Math.abs(number) >= 1_000) {
            number /= 1_000;
            suffix = "K";
        }
        return suffix.isEmpty() ? Long.toString(value) :
                ("%." + Math.max(0, precision - 1) + "f%s").formatted(number, suffix);
    }
}
