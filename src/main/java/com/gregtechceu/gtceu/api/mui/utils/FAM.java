package com.gregtechceu.gtceu.api.mui.utils;

import net.minecraft.util.Mth;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * A helper class providing math operations on 1D float arrays similar to numpy.
 */
public class FAM {

    public static final float[] EMPTY = new float[0];

    /**
     * Creates an array of length n filled with zeros.
     *
     * @param n length
     * @return zero filled array
     */
    public static float[] zeros(int n) {
        return new float[n];
    }

    /**
     * Creates an array of length n filled with f.
     *
     * @param n length
     * @param f fill value
     * @return f filled array
     */
    public static float[] full(int n, float f) {
        float[] arr = new float[n];
        Arrays.fill(arr, f);
        return arr;
    }

    /**
     * Creates an array of length n filled with ones.
     *
     * @param n length
     * @return one filled array
     */
    public static float[] ones(int n) {
        return full(n, 1);
    }

    public static float[] copyInto(float[] src, float @Nullable [] res) {
        if (src == res) return res;
        if (res == null) res = new float[src.length];
        int n = Math.min(src.length, res.length);
        System.arraycopy(src, 0, res, 0, n);
        return res;
    }

    public static float[] subArray(float[] src, int start, int length) {
        float[] res = new float[length];
        System.arraycopy(src, start, res, 0, length);
        return res;
    }

    public static float[] linspace(float start, float stop) {
        return linspace(start, stop, 50, true);
    }

    public static float[] linspace(float start, float stop, boolean includeEndpoint) {
        return linspace(start, stop, 50, includeEndpoint);
    }

    public static float[] linspace(float start, float stop, int n) {
        return linspace(start, stop, n, true);
    }

    /**
     * Creates an evenly spaced array over a specified interval.
     *
     * @param start           start of interval
     * @param stop            stop of interval
     * @param n               sample size = array length
     * @param includeEndpoint true if stop should be included at the end of the array
     * @return evenly spaced array over interval
     */
    public static float[] linspace(float start, float stop, int n, boolean includeEndpoint) {
        float[] arr = new float[n];
        float step = (stop - start) / (includeEndpoint ? n + 1 : n);
        int s = n;
        if (includeEndpoint) {
            arr[n - 1] = stop;
            s--;
        }
        for (int i = 0; i < s; i++) {
            arr[i] = start + step * i;
        }
        return arr;
    }

    public static float[] arange(float stop, float step) {
        return arange(0, stop, step);
    }

    public static float[] arange(float start, float stop, float step) {
        float[] arr = new float[(int) Math.ceil((stop - start) / step)];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = start + step * i;
        }
        return arr;
    }

    /**
     * Returns the index of the largest value in the array. If array is empty, -1 is returned.
     *
     * @param arr array
     * @return index of largest value
     */
    public static int argMax(float[] arr) {
        if (arr.length == 0) return -1;
        if (arr.length == 1) return 0;
        if (arr.length == 2) return arr[0] >= arr[1] ? 0 : 1;
        int index = 0;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > arr[index]) index = i;
        }
        return index;
    }

    /**
     * Returns the index of the smallest value in the array. If array is empty, -1 is returned.
     *
     * @param arr array
     * @return index of smallest value
     */
    public static int argMin(float[] arr) {
        if (arr.length == 0) return -1;
        if (arr.length == 1) return 0;
        if (arr.length == 2) return arr[0] <= arr[1] ? 0 : 1;
        int index = 0;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < arr[index]) index = i;
        }
        return index;
    }

    /**
     * Returns the largest value in the array. If array is empty, 0 is returned.
     *
     * @param arr array
     * @return largest value
     */
    public static float max(float[] arr) {
        int i = argMax(arr);
        return i < 0 ? 0 : arr[i];
    }

    /**
     * Returns the smallest value in the array. If array is empty, 0 is returned.
     *
     * @param arr array
     * @return smallest value
     */
    public static float min(float[] arr) {
        int i = argMin(arr);
        return i < 0 ? 0 : arr[i];
    }

    /**
     * Adds an operand to every element of the source array.
     *
     * @param src source array
     * @param op  operand
     * @param res result array. If this is null a new array is created. This can be the same as src.
     * @return result array
     */
    public static float[] plus(float[] src, float op, float @Nullable [] res) {
        return applyEach(src, v -> v + op, res);
    }

    /**
     * Adds each element of the operand to the element at the corresponding index of the source array.
     *
     * @param src source array
     * @param op  operands
     * @param res result array. If this is null a new array is created. This can be the same as src.
     * @return result array
     */
    public static float[] plus(float[] src, float[] op, float @Nullable [] res) {
        return applyEach(src, op, Float::sum, res);
    }

    /**
     * Multiplies an operand with every element of the source array.
     *
     * @param src source array
     * @param op  operand
     * @param res result array. If this is null a new array is created. This can be the same as src.
     * @return result array
     */
    public static float[] mult(float[] src, float op, float @Nullable [] res) {
        return applyEach(src, v -> v * op, res);
    }

    public static float[] mult(float[] src, float[] op, float @Nullable [] res) {
        return applyEach(src, op, (v, op1) -> v * op1, res);
    }

    public static float[] div(float[] src, float op, float @Nullable [] res) {
        return mult(src, 1 / op, res);
    }

    public static float[] div(float[] src, float[] op, float @Nullable [] res) {
        return applyEach(src, op, (v, op1) -> v / op1, res);
    }

    public static float[] diff(float[] src) {
        if (src.length < 2) return EMPTY;
        if (src.length == 2) return new float[] { src[1] - src[0] };
        float[] res = new float[src.length - 1];
        for (int i = 0; i < res.length; i++) {
            res[i] = src[i + 1] - src[i];
        }
        return res;
    }

    public static float[] applyEach(float[] src, UnaryFloatOperator op, float @Nullable [] res) {
        if (res == null) res = new float[src.length];
        int n = Math.min(src.length, res.length);
        for (int i = 0; i < n; i++) res[i] = op.apply(src[i]);
        return res;
    }

    public static float[] applyEach(float[] src, float[] operands, BinaryFloatOperator op, float @Nullable [] res) {
        if (src.length != operands.length)
            throw new IllegalArgumentException("Can't apply operator to operands of different size.");
        if (res == null) res = new float[src.length];
        int n = Math.min(src.length, res.length);
        for (int i = 0; i < n; i++) res[i] = op.apply(src[i], operands[i]);
        return res;
    }

    public static float[] applyEach(float[] src, float[] operands1, float[] operands2, TernaryFloatOperator op,
                                    float @Nullable [] res) {
        if (src.length != operands1.length || src.length != operands2.length) {
            throw new IllegalArgumentException("Can't apply operator to operands of different size.");
        }
        if (res == null) res = new float[src.length];
        int n = Math.min(src.length, res.length);
        for (int i = 0; i < n; i++) res[i] = op.apply(src[i], operands1[i], operands2[i]);
        return res;
    }

    public static float[] applyEach(float[] src, float[][] operands, NFloatOperator op, float @Nullable [] res) {
        if (src.length != operands.length) {
            throw new IllegalArgumentException("Can't apply operator to operands of different size.");
        }
        if (res == null) res = new float[src.length];
        int n = Math.min(src.length, res.length);
        for (int i = 0; i < n; i++) res[i] = op.apply(src[i], operands[i]);
        return res;
    }

    public static float[] abs(float[] src, float @Nullable [] res) {
        return applyEach(src, Math::abs, res);
    }

    public static float[] sin(float[] src, float @Nullable [] res) {
        return applyEach(src, Mth::sin, res);
    }

    public static float[] cos(float[] src, float @Nullable [] res) {
        return applyEach(src, Mth::cos, res);
    }

    public static float[] tan(float[] src, float @Nullable [] res) {
        return applyEach(src, (r) -> (float) Math.tan(r), res);
    }

    public static float[] clamp(float[] src, float min, float max, float @Nullable [] res) {
        return applyEach(src, v -> Mth.clamp(v, min, max), res);
    }

    public static float[] polynomial(float[] src, float[] coeff, float @Nullable [] res) {
        if (coeff.length == 0) return copyInto(src, res);
        if (coeff.length == 1) return plus(src, coeff[0], res);
        return applyEach(src, x -> {
            float y = 0;
            y += coeff[0];
            if (coeff.length == 2) return y + x * coeff[1];
            for (int i = 1; i < coeff.length; i++) {
                y += (float) (Math.pow(x, i) * coeff[i]);
            }
            return y;
        }, res);
    }

    public interface UnaryFloatOperator {

        float apply(float v);
    }

    public interface BinaryFloatOperator {

        float apply(float v, float op);
    }

    public interface TernaryFloatOperator {

        float apply(float v, float op1, float op2);
    }

    public interface NFloatOperator {

        float apply(float v, float[] op);
    }
}
