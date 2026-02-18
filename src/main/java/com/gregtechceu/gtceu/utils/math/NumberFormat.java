package com.gregtechceu.gtceu.utils.math;

import com.gregtechceu.gtceu.utils.GTMath;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * A number formatter for large, small, positive, negative, integers and decimals with adjustable format parameters.
 * Test results can be seen at test/java/com.cleanroommc.modularui.FormatTest.
 */
public class NumberFormat {

    public static final BigDecimal TEN_THOUSAND = new BigDecimal(10_000);

    public static final Params DEFAULT = paramsBuilder()
            .roundingMode(RoundingMode.HALF_UP)
            .maxLength(4)
            .considerMinusForLength(false)
            .considerDecimalSeparatorForLength(false)
            .considerOnlyDecimalsForLength(false)
            .considerSuffixForLength(true)
            .spaceAfterNumber(false)
            .build();

    public static final Params DECIMALS_3 = DEFAULT.copyToBuilder()
            .considerSuffixForLength(false)
            .considerOnlyDecimalsForLength(true)
            .maxLength(3)
            .build();

    public static final Params AMOUNT_TEXT = DEFAULT.copyToBuilder()
            .roundingMode(RoundingMode.DOWN)
            .build();

    public static Params params(DecimalFormat format, int maxLength, boolean considerOnlyDecimalsForLength,
                                boolean considerDecimalSeparatorForLength, boolean considerMinusForLength,
                                boolean considerSuffixForLength,
                                boolean spaceAfterNumber) {
        return new Params(format, maxLength, considerOnlyDecimalsForLength, considerDecimalSeparatorForLength,
                considerMinusForLength, considerSuffixForLength, spaceAfterNumber);
    }

    public static ParamsBuilder paramsBuilder() {
        return new ParamsBuilder();
    }

    public static class Params {

        public final DecimalFormat format;
        public final int maxLength;
        public final boolean considerOnlyDecimalsForLength;
        public final boolean considerDecimalSeparatorForLength;
        public final boolean considerMinusForLength;
        public final boolean considerSuffixForLength;
        public final boolean spaceAfterNumber;

        public Params(DecimalFormat format, int maxLength, boolean considerOnlyDecimalsForLength,
                      boolean considerDecimalSeparatorForLength,
                      boolean considerMinusForLength, boolean considerSuffixForLength, boolean spaceAfterNumber) {
            this.format = format;
            this.maxLength = maxLength;
            this.considerOnlyDecimalsForLength = considerOnlyDecimalsForLength;
            this.considerDecimalSeparatorForLength = considerDecimalSeparatorForLength;
            this.considerMinusForLength = considerMinusForLength;
            this.considerSuffixForLength = considerSuffixForLength;
            this.spaceAfterNumber = spaceAfterNumber;
            if (!this.considerOnlyDecimalsForLength && this.maxLength < 4) {
                throw new IllegalArgumentException("Max length must be at least 4 characters");
            }
        }

        public ParamsBuilder copyToBuilder() {
            DecimalFormat format = (DecimalFormat) this.format.clone();
            format.setMinimumFractionDigits(this.format.getMinimumFractionDigits());
            format.setMaximumFractionDigits(this.format.getMaximumFractionDigits());
            format.setMinimumIntegerDigits(this.format.getMinimumIntegerDigits());
            format.setMaximumIntegerDigits(this.format.getMaximumIntegerDigits());
            format.setRoundingMode(this.format.getRoundingMode());
            format.setGroupingSize(this.format.getGroupingSize());
            format.setGroupingUsed(this.format.isGroupingUsed());
            return new ParamsBuilder()
                    .format(format)
                    .maxLength(this.maxLength)
                    .considerOnlyDecimalsForLength(this.considerOnlyDecimalsForLength)
                    .considerDecimalSeparatorForLength(this.considerDecimalSeparatorForLength)
                    .considerMinusForLength(this.considerMinusForLength)
                    .considerSuffixForLength(this.considerSuffixForLength)
                    .spaceAfterNumber(this.spaceAfterNumber);
        }

        public String format(double number) {
            return NumberFormat.format(number, this);
        }

        public String format(BigDecimal number) {
            return NumberFormat.format(number, this);
        }
    }

    public static class ParamsBuilder {

        private DecimalFormat format;
        private int maxLength;
        private boolean considerOnlyDecimalsForLength;
        private boolean considerDecimalSeparatorForLength;
        private boolean considerMinusForLength;
        private boolean considerSuffixForLength;
        private boolean spaceAfterNumber;

        private DecimalFormat checkFormat() {
            if (this.format == null) {
                this.format = new DecimalFormat("0.###");
            }
            return this.format;
        }

        public ParamsBuilder format(DecimalFormat format) {
            this.format = format;
            return this;
        }

        public ParamsBuilder roundingMode(RoundingMode roundingMode) {
            checkFormat().setRoundingMode(roundingMode);
            return this;
        }

        public ParamsBuilder decimalFormatSymbols(DecimalFormatSymbols symbols) {
            checkFormat().setDecimalFormatSymbols(symbols);
            return this;
        }

        public ParamsBuilder decimalSeparator(char c) {
            DecimalFormatSymbols symbols = checkFormat().getDecimalFormatSymbols();
            symbols.setDecimalSeparator(c);
            checkFormat().setDecimalFormatSymbols(symbols);
            return this;
        }

        public ParamsBuilder maxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public ParamsBuilder considerOnlyDecimalsForLength(boolean considerOnlyDecimalsForLength) {
            this.considerOnlyDecimalsForLength = considerOnlyDecimalsForLength;
            return this;
        }

        public ParamsBuilder considerDecimalSeparatorForLength(boolean considerDecimalSeparatorForLength) {
            this.considerDecimalSeparatorForLength = considerDecimalSeparatorForLength;
            return this;
        }

        public ParamsBuilder considerMinusForLength(boolean considerMinusForLength) {
            this.considerMinusForLength = considerMinusForLength;
            return this;
        }

        public ParamsBuilder considerSuffixForLength(boolean considerSuffixForLength) {
            this.considerSuffixForLength = considerSuffixForLength;
            return this;
        }

        public ParamsBuilder spaceAfterNumber(boolean spaceAfterNumber) {
            this.spaceAfterNumber = spaceAfterNumber;
            return this;
        }

        public Params build() {
            return new Params(checkFormat(), this.maxLength, this.considerOnlyDecimalsForLength,
                    this.considerDecimalSeparatorForLength,
                    this.considerMinusForLength, this.considerSuffixForLength, spaceAfterNumber);
        }
    }

    public static String format(double number, Params params) {
        boolean negative = number < 0;
        int maxLength = params.maxLength;
        if (negative) {
            number = -number;
            if (params.considerMinusForLength) maxLength--;
        }
        String formattedNumber = formatInternal(number, maxLength, params);
        if (negative) formattedNumber = "-" + formattedNumber;
        return formattedNumber;
    }

    public static String format(BigDecimal number, Params params) {
        boolean negative = number.compareTo(BigDecimal.ZERO) < 0;
        int maxLength = params.maxLength;
        if (negative) {
            number = number.negate();
            if (params.considerMinusForLength) maxLength--;
        }
        String formattedNumber = formatInternal(number, maxLength, params);
        if (negative) formattedNumber = "-" + formattedNumber;
        return formattedNumber;
    }

    public static String formatFromUnit(double number, SIPrefix unit, Params params) {
        return format(number * unit.factor, params);
    }

    public static SIPrefix findBestPrefix(double number) {
        if (Double.isNaN(number)) return SIPrefix.One;
        if (Double.isInfinite(number)) return SIPrefix.Infinite;
        number = Math.abs(number);
        if (number == 0 || (number >= 1 && number < 10_000)) return SIPrefix.One;
        SIPrefix[] high = SIPrefix.HIGH;
        SIPrefix[] low = SIPrefix.LOW;
        int n = high.length - 1;
        SIPrefix prefix;
        if (number >= 10_000) {
            int index;
            for (index = 0; index < n; index++) {
                if (number < high[index + 1].factor) {
                    break;
                }
            }
            prefix = high[index];
        } else {
            int index;
            for (index = 0; index < n; index++) {
                if (number >= low[index].factor) {
                    break;
                }
            }
            prefix = low[index];
        }
        return prefix;
    }

    public static SIPrefix findBestPrefix(BigDecimal number) {
        number = number.abs();
        if ((number.compareTo(BigDecimal.ONE) >= 0 && number.compareTo(TEN_THOUSAND) < 0) ||
                number.equals(BigDecimal.ZERO)) {
            return SIPrefix.One;
        }
        SIPrefix[] high = SIPrefix.HIGH;
        SIPrefix[] low = SIPrefix.LOW;
        int n = high.length - 1;
        SIPrefix prefix;
        if (number.compareTo(TEN_THOUSAND) >= 0) {
            int index;
            for (index = 0; index < n; index++) {
                if (number.compareTo(high[index + 1].bigFactor) < 0) {
                    break;
                }
            }
            prefix = high[index];
        } else {
            int index;
            for (index = 0; index < n; index++) {
                if (number.compareTo(low[index].bigFactor) >= 0) {
                    break;
                }
            }
            prefix = low[index];
        }
        return prefix;
    }

    private static String formatInternal(double number, int maxLength, Params params) {
        if (Double.isNaN(number)) return "NaN";
        SIPrefix prefix = findBestPrefix(number);
        if (prefix.infiniteLike) return prefix.stringSymbol;
        return formatToString(number * prefix.oneOverFactor, prefix.symbol, maxLength, params);
    }

    private static String formatToString(double value, char prefix, int maxLength, Params params) {
        if (params.considerSuffixForLength && prefix != Character.MIN_VALUE) {
            maxLength--;
            if (params.spaceAfterNumber) maxLength--;
        }
        if (params.considerDecimalSeparatorForLength) maxLength--;
        if (!params.considerOnlyDecimalsForLength) {
            if (value % 1 > 0) {
                maxLength -= GTMath.intPlaces(value);
            }
        }
        int m1 = params.format.getMaximumFractionDigits();
        int m2 = params.format.getMinimumFractionDigits();
        params.format.setMaximumFractionDigits(maxLength);
        String s = params.format.format(value);
        params.format.setMaximumFractionDigits(m1);
        params.format.setMinimumFractionDigits(m2);

        if (prefix != Character.MIN_VALUE) {
            if (params.spaceAfterNumber) s += ' ';
            s += prefix;
        }
        return s;
    }

    private static String formatInternal(BigDecimal number, int maxLength, Params params) {
        SIPrefix prefix = findBestPrefix(number);
        if (prefix.infiniteLike) return prefix.stringSymbol;
        return formatToString(number.multiply(prefix.bigOneOverFactor), prefix.symbol, maxLength, params);
    }

    private static String formatToString(BigDecimal value, char prefix, int maxLength, Params params) {
        if (params.considerSuffixForLength && prefix != Character.MIN_VALUE) {
            maxLength--;
            if (params.spaceAfterNumber) maxLength--;
        }
        if (params.considerDecimalSeparatorForLength) maxLength--;
        if (!params.considerOnlyDecimalsForLength) {
            if (value.remainder(BigDecimal.ONE).signum() > 0) {
                maxLength -= GTMath.intPlaces(value);
            }
        }
        int m1 = params.format.getMaximumFractionDigits();
        int m2 = params.format.getMinimumFractionDigits();
        params.format.setMaximumFractionDigits(maxLength);
        String s = params.format.format(value);
        params.format.setMaximumFractionDigits(m1);
        params.format.setMinimumFractionDigits(m2);

        if (prefix != Character.MIN_VALUE) {
            if (params.spaceAfterNumber) s += ' ';
            s += prefix;
        }
        return s;
    }

    public static String formatNanos(long nanos) {
        return DECIMALS_3.format(nanos * SIPrefix.Nano.factor);
    }
}
