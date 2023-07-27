package com.gregtechceu.gtceu.utils;

public class RedstoneUtil {

    /**
     * Compares a value against a min and max, with an option to invert the logic
     *
     * @param value      value to be compared
     * @param maxValue   the max that the value can be
     * @param minValue   the min that the value can be
     * @param isInverted whether to invert the logic of this method
     * @return an int from 0 (value <= min) to 15 (value >= max) normally, with a ratio when the value is between min and max
     */
    public static int computeRedstoneBetweenValues(long value, float maxValue, float minValue, boolean isInverted) {
        if (value >= maxValue) {
            return isInverted ? 0 : 15; // value above maxValue should normally be 15, otherwise 0
        } else if (value <= minValue) {
            return isInverted ? 15 : 0; // value below minValue should normally be 0, otherwise 15
        }

        float ratio;
        if (!isInverted) {
            ratio = 15 * (value - minValue) / (maxValue - minValue); // value closer to max results in higher output
        } else {
            ratio = 15 * (maxValue - value) / (maxValue - minValue); // value closer to min results in higher output
        }

        return Math.round(ratio);
    }

    /**
     * Compares a value against a min and max, with an option to invert the logic. Has latching functionality.
     *
     * @param value    value to be compared
     * @param maxValue the max that the value can be
     * @param minValue the min that the value can be
     * @param output   the output value the function modifies
     * @return returns the modified output value
     */
    public static int computeLatchedRedstoneBetweenValues(float value, float maxValue, float minValue, boolean isInverted, int output) {
        if (value >= maxValue) {
            output = !isInverted ? 0 : 15; // value above maxValue should normally be 0, otherwise 15
        } else if (value <= minValue) {
            output = !isInverted ? 15 : 0; // value below minValue should normally be 15, otherwise 0
        }
        return output;
    }

    /**
     * Compares a current against max, with an option to invert the logic.
     *
     * @param current    value to be compared
     * @param max        the max that value can be
     * @param isInverted whether to invert the logic of this method
     * @return value 0 to 15
     * @throws ArithmeticException when max is 0
     */
    public static int computeRedstoneValue(long current, long max, boolean isInverted) throws ArithmeticException {
        int outputAmount = (int) (15 * current / max);

        if (isInverted) {
            outputAmount = 15 - outputAmount;
        }

        return outputAmount;
    }
}
