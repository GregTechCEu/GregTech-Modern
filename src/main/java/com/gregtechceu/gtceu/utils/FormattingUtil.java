package com.gregtechceu.gtceu.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import static net.minecraft.ChatFormatting.YELLOW;

@SuppressWarnings("UnnecessaryUnicodeEscape")
public class FormattingUtil {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.ROOT);
    public static final DecimalFormat DECIMAL_FORMAT_0F = new DecimalFormat(",###");
    public static final DecimalFormat DECIMAL_FORMAT_1F = new DecimalFormat("#,##0.#");
    public static final DecimalFormat DECIMAL_FORMAT_2F = new DecimalFormat("#,##0.##");
    public static final DecimalFormat DECIMAL_FORMAT_SIC = new DecimalFormat("0E00");
    public static final DecimalFormat DECIMAL_FORMAT_SIC_2F = new DecimalFormat("0.00E00");

    private static final int SMALL_DOWN_NUMBER_BASE = '\u2080';
    private static final int SMALL_UP_NUMBER_BASE = '\u2070';
    private static final int SMALL_UP_NUMBER_ONE = '\u00B9';
    private static final int SMALL_UP_NUMBER_TWO = '\u00B2';
    private static final int SMALL_UP_NUMBER_THREE = '\u00B3';
    private static final int NUMBER_BASE = '0';

    public static String toSmallUpNumbers(String string) {
        return checkNumbers(string, SMALL_UP_NUMBER_BASE);
    }

    public static String toSmallDownNumbers(String string) {
        return checkNumbers(string, SMALL_DOWN_NUMBER_BASE);
    }

    @NotNull
    private static String checkNumbers(String string, int startIndex) {
        char[] charArray = string.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            int relativeIndex = charArray[i] - NUMBER_BASE;
            if (relativeIndex >= 0 && relativeIndex <= 9) {
                // is superscript
                if (startIndex == SMALL_UP_NUMBER_BASE) {
                    if (relativeIndex == 1) {
                        charArray[i] = SMALL_UP_NUMBER_ONE;
                        continue;
                    } else if (relativeIndex == 2) {
                        charArray[i] = SMALL_UP_NUMBER_TWO;
                        continue;
                    } else if (relativeIndex == 3) {
                        charArray[i] = SMALL_UP_NUMBER_THREE;
                        continue;
                    }
                }
                int newChar = startIndex + relativeIndex;
                charArray[i] = (char) newChar;
            }
        }
        return new String(charArray);
    }

    /**
     * Does almost the same thing as {@code UPPER_CAMEL.to(LOWER_UNDERSCORE, string)},
     * but it also inserts underscores between words and numbers.
     *
     * @param string Any string with ASCII characters.
     * @return A string that is all lowercase, with underscores inserted before word/number boundaries:
     * 
     *         <pre>
     *         <br>{@code "maragingSteel300" -> "maraging_steel_300"}
     *         <br>{@code "gtceu:maraging_steel_300" -> "gtceu:maraging_steel_300"}
     *         <br>{@code "maragingSteel_300" -> "maraging_steel_300"}
     *         <br>{@code "maragingSTEEL_300" -> "maraging_steel_300"}
     *         <br>{@code "MARAGING_STEEL_300" -> "maraging_steel_300"}
     * </pre>
     */
    public static String toLowerCaseUnderscore(String string) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char curChar = string.charAt(i);
            result.append(Character.toLowerCase(curChar));
            if (i == string.length() - 1) break;

            char nextChar = string.charAt(i + 1);
            if (curChar == '_' || nextChar == '_') continue;
            boolean nextIsUpper = Character.isUpperCase(nextChar);
            if (Character.isUpperCase(curChar) && nextIsUpper) continue;
            if (nextIsUpper || Character.isDigit(curChar) ^ Character.isDigit(nextChar)) result.append('_');
        }
        return result.toString();
    }

    /**
     * @deprecated use {@link FormattingUtil#toLowerCaseUnderscore(String) toLowerCaseUnderscore} instead.
     */
    @ApiStatus.Obsolete(since = "7.0.0")
    @Deprecated(since = "7.0.0")
    public static String toLowerCaseUnder(String string) {
        return toLowerCaseUnderscore(string);
    }

    /**
     * Check if {@code string} has any uppercase characters.
     *
     * @param string the string to check
     * @return if the string has any uppercase characters.
     */
    public static boolean hasUpperCase(String string) {
        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if (Character.isUpperCase(ch)) return true;
        }
        return false;
    }

    /**
     * apple_orange.juice => Apple Orange (Juice)
     */
    public static String toEnglishName(Object internalName) {
        return Arrays.stream(internalName.toString().toLowerCase(Locale.ROOT).split("_"))
                .map(StringUtils::capitalize)
                .collect(Collectors.joining(" "));
    }

    /**
     * Converts integers to roman numerals.
     * e.g. 17 => XVII, 2781 => MMDCCLXXXI
     */
    public static String toRomanNumeral(int number) {
        return "I".repeat(number)
                .replace("IIIII", "V")
                .replace("IIII", "IV")
                .replace("VV", "X")
                .replace("VIV", "IX")
                .replace("XXXXX", "L")
                .replace("XXXX", "XL")
                .replace("LL", "C")
                .replace("LXL", "XC")
                .replace("CCCCC", "D")
                .replace("CCCC", "CD")
                .replace("DD", "M")
                .replace("DCD", "CM");
    }

    /**
     * Does almost the same thing as LOWER_UNDERSCORE.to(UPPER_CAMEL, string), but it also removes underscores before
     * numbers.
     *
     * @param string Any string with ASCII characters.
     * @return A string that is all lowercase, with underscores inserted before word/number boundaries:
     *         "maraging_steel_300" -> "maragingSteel300"
     */
    public static String lowerUnderscoreToUpperCamel(String string) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == '_')
                continue;
            if (i == 0 || string.charAt(i - 1) == '_') {
                result.append(Character.toUpperCase(string.charAt(i)));
            } else {
                result.append(string.charAt(i));
            }
        }
        return result.toString();
    }

    public static String formatPercent(double number) {
        return String.format("%,.2f", number);
    }

    /** To avoids (un)boxing. */
    public static String formatNumbers(int number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String formatNumbers(long number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String formatNumbers(double number) {
        return NUMBER_FORMAT.format(number);
    }

    /** Allows for formatting Long, Integer, Short, Byte, Number, AtomicInteger, AtomicLong, and BigInteger. */
    public static String formatNumbers(Object number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String formatNumberReadable(long number) {
        return formatNumberReadable(number, false);
    }

    public static String formatNumberReadable(long number, boolean milli) {
        return formatNumberReadable(number, milli, DECIMAL_FORMAT_1F, null);
    }

    public static String formatNumberReadable2F(double number, boolean milli) {
        return formatNumberReadable(number, milli, DECIMAL_FORMAT_2F, null);
    }

    /**
     * Format number in engineering notation with SI prefixes [m, k, M, G, T, P, E, Z]
     *
     * @param number Number to format
     * @param milli  Whether the passed number is already in millis (e.g., mB)
     * @param fmt    Formatter to use for compacted number
     * @param unit   Optional unit to append
     * @return Compacted number with SI prefix
     */
    public static String formatNumberReadable(double number, boolean milli, NumberFormat fmt, @Nullable String unit) {
        StringBuilder sb = new StringBuilder();
        if (number < 0) {
            number = -number;
            sb.append('-');
        }

        if (milli && number >= 1e3) {
            milli = false;
            number /= 1e3;
        }

        int exp = 0;
        if (number >= 1e3) {
            exp = (int) Math.log10(number) / 3;
            if (exp > 7) exp = 7;
            if (exp > 0) number /= Math.pow(1e3, exp);
        }

        sb.append(fmt.format(number));
        if (exp > 0) sb.append("kMGTPEZ".charAt(exp - 1));
        else if (milli && number != 0) sb.append('m');

        if (unit != null) sb.append(unit);
        return sb.toString();
    }

    public static String formatNumberOrSic(BigInteger number, BigInteger threshold) {
        return number.compareTo(threshold) > 0 ? DECIMAL_FORMAT_SIC_2F.format(number) : formatNumbers(number);
    }

    public static String formatBuckets(long mB) {
        return formatNumberReadable(mB, true, DECIMAL_FORMAT_2F, "B");
    }

    @NotNull
    public static String formatNumber2Places(float number) {
        return DECIMAL_FORMAT_2F.format(number);
    }

    @NotNull
    public static String formatNumber2Places(double number) {
        return DECIMAL_FORMAT_2F.format(number);
    }

    public static Component formatPercentage2Places(String langKey, float percentage) {
        return Component.translatable(langKey, formatNumber2Places(percentage)).withStyle(YELLOW);
    }

    public static void combineComponents(MutableComponent c1, Component c2) {
        if (!isEmptyComponent(c1) && !isEmptyComponent(c2)) {
            c1.append(", ").append(c2);
        } else {
            c1.append(c2);
        }
    }

    private static boolean isEmptyComponent(Component component) {
        return component.getContents() == ComponentContents.EMPTY && component.getSiblings().isEmpty();
    }
}
