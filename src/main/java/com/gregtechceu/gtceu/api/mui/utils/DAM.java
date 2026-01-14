package com.gregtechceu.gtceu.api.mui.utils;

import net.minecraft.util.Mth;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * A helper class providing math operations on 1D double arrays similar to numpy.
 * DAM stands for DoubleArrayMath.
 */
public class DAM {

    public static final double[] EMPTY = new double[0];

    /**
     * Creates an array of length n filled with zeros.
     *
     * @param n length
     * @return zero filled array
     */
    public static double[] zeros(int n) {
        return new double[n];
    }

    /**
     * Creates an array of length n filled with f.
     *
     * @param n length
     * @param f fill value
     * @return f filled array
     */
    public static double[] full(int n, double f) {
        double[] arr = new double[n];
        Arrays.fill(arr, f);
        return arr;
    }

    /**
     * Creates an array of length n filled with ones.
     *
     * @param n length
     * @return one filled array
     */
    public static double[] ones(int n) {
        return full(n, 1);
    }

    public static double[] copyInto(double[] src, double @Nullable [] res) {
        if (src == res) return res;
        if (res == null) res = new double[src.length];
        int n = Math.min(src.length, res.length);
        System.arraycopy(src, 0, res, 0, n);
        return res;
    }

    public static double[] subArray(double[] src, int start, int length) {
        double[] res = new double[length];
        System.arraycopy(src, start, res, 0, length);
        return res;
    }

    public static double[] ofFloats(float[] src) {
        double[] res = new double[src.length];
        for (int i = 0; i < src.length; i++) res[i] = src[i];
        return res;
    }

    public static double[] ofInts(int[] src) {
        double[] res = new double[src.length];
        for (int i = 0; i < src.length; i++) res[i] = src[i];
        return res;
    }

    public static double[] ofLongs(long[] src) {
        double[] res = new double[src.length];
        for (int i = 0; i < src.length; i++) res[i] = src[i];
        return res;
    }

    public static double[] linspace(double start, double stop) {
        return linspace(start, stop, 50, true);
    }

    public static double[] linspace(double start, double stop, boolean includeEndpoint) {
        return linspace(start, stop, 50, includeEndpoint);
    }

    public static double[] linspace(double start, double stop, int n) {
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
    public static double[] linspace(double start, double stop, int n, boolean includeEndpoint) {
        double[] arr = new double[n];
        double step = (stop - start) / (includeEndpoint ? n + 1 : n);
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

    public static double[] arange(double stop, double step) {
        return arange(0, stop, step);
    }

    public static double[] arange(double start, double stop, double step) {
        double[] arr = new double[(int) Math.ceil((stop - start) / step)];
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
    public static int argMax(double[] arr) {
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
    public static int argMin(double[] arr) {
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
    public static double max(double[] arr) {
        int i = argMax(arr);
        return i < 0 ? 0 : arr[i];
    }

    /**
     * Returns the smallest value in the array. If array is empty, 0 is returned.
     *
     * @param arr array
     * @return smallest value
     */
    public static double min(double[] arr) {
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
    public static double[] plus(double[] src, double op, double @Nullable [] res) {
        return applyEach(src, v -> v + op, res);
    }

    public static double[] plusMut(double[] src, double op) {
        return plus(src, op, src);
    }

    /**
     * Adds each element of the operand to the element at the corresponding index of the source array.
     *
     * @param src source array
     * @param op  operands
     * @param res result array. If this is null a new array is created. This can be the same as src.
     * @return result array
     */
    public static double[] plus(double[] src, double[] op, double @Nullable [] res) {
        return applyEach(src, op, Double::sum, res);
    }

    public static double[] plusMut(double[] src, double[] op) {
        return plus(src, op, src);
    }

    /**
     * Multiplies an operand with every element of the source array.
     *
     * @param src source array
     * @param op  operand
     * @param res result array. If this is null a new array is created. This can be the same as src.
     * @return result array
     */
    public static double[] mult(double[] src, double op, double @Nullable [] res) {
        return applyEach(src, v -> v * op, res);
    }

    public static double[] multMut(double[] src, double op) {
        return mult(src, op, src);
    }

    public static double[] mult(double[] src, double[] op, double @Nullable [] res) {
        return applyEach(src, op, (v, op1) -> v * op1, res);
    }

    public static double[] multMut(double[] src, double[] op) {
        return mult(src, op, src);
    }

    public static double[] div(double[] src, double op, double @Nullable [] res) {
        return mult(src, 1 / op, res);
    }

    public static double[] divMut(double[] src, double op) {
        return div(src, op, src);
    }

    public static double[] div(double[] src, double[] op, double @Nullable [] res) {
        return applyEach(src, op, (v, op1) -> v / op1, res);
    }

    public static double[] divMut(double[] src, double[] op) {
        return div(src, op, src);
    }

    public static double[] reciprocal(double a, double[] b, double @Nullable [] res) {
        return applyEach(b, v -> a / v, res);
    }

    public static double[] square(double[] src, double @Nullable [] res) {
        return applyEach(src, v -> v * v, res);
    }

    public static double[] cube(double[] src, double @Nullable [] res) {
        return applyEach(src, v -> v * v * v, res);
    }

    public static double[] pow(double[] src, double op, double @Nullable [] res) {
        return applyEach(src, v -> (double) Math.pow(v, op), res);
    }

    public static double[] diff(double[] src) {
        if (src.length < 2) return EMPTY;
        if (src.length == 2) return new double[] { src[1] - src[0] };
        double[] res = new double[src.length - 1];
        for (int i = 0; i < res.length; i++) {
            res[i] = src[i + 1] - src[i];
        }
        return res;
    }

    public static double[] applyEach(double[] src, UnaryDoubleOperator op, double @Nullable [] res) {
        if (res == null) res = new double[src.length];
        int n = Math.min(src.length, res.length);
        for (int i = 0; i < n; i++) res[i] = op.apply(src[i]);
        return res;
    }

    public static double[] applyEach(double[] src, double[] operands, BinaryDoubleOperator op,
                                     double @Nullable [] res) {
        if (src.length != operands.length)
            throw new IllegalArgumentException("Can't apply operator to operands of different size.");
        if (res == null) res = new double[src.length];
        int n = Math.min(src.length, res.length);
        for (int i = 0; i < n; i++) res[i] = op.apply(src[i], operands[i]);
        return res;
    }

    public static double[] applyEach(double[] src, double[] operands1, double[] operands2, TernaryDoubleOperator op,
                                     double @Nullable [] res) {
        if (src.length != operands1.length || src.length != operands2.length) {
            throw new IllegalArgumentException("Can't apply operator to operands of different size.");
        }
        if (res == null) res = new double[src.length];
        int n = Math.min(src.length, res.length);
        for (int i = 0; i < n; i++) res[i] = op.apply(src[i], operands1[i], operands2[i]);
        return res;
    }

    public static double[] applyEach(double[] src, double[][] operands, NDoubleOperator op, double @Nullable [] res) {
        if (src.length != operands.length) {
            throw new IllegalArgumentException("Can't apply operator to operands of different size.");
        }
        if (res == null) res = new double[src.length];
        int n = Math.min(src.length, res.length);
        for (int i = 0; i < n; i++) res[i] = op.apply(src[i], operands[i]);
        return res;
    }

    public static double[] abs(double[] src, double @Nullable [] res) {
        return applyEach(src, Math::abs, res);
    }

    public static double[] sin(double[] src, double @Nullable [] res) {
        return applyEach(src, Math::sin, res);
    }

    public static double[] cos(double[] src, double @Nullable [] res) {
        return applyEach(src, Math::cos, res);
    }

    public static double[] tan(double[] src, double @Nullable [] res) {
        return applyEach(src, Math::tan, res);
    }

    public static double[] clamp(double[] src, double min, double max, double @Nullable [] res) {
        return applyEach(src, v -> Mth.clamp(v, min, max), res);
    }

    public static double[] polynomial(double[] src, double[] coeff, double @Nullable [] res) {
        if (coeff.length == 0) return copyInto(src, res);
        if (coeff.length == 1) return plus(src, coeff[0], res);
        return applyEach(src, x -> {
            double y = 0;
            y += coeff[0];
            if (coeff.length == 2) return y + x * coeff[1];
            for (int i = 1; i < coeff.length; i++) {
                y += (double) (Math.pow(x, i) * coeff[i]);
            }
            return y;
        }, res);
    }

    public static double reduce(double[] src, BinaryDoubleOperator op) {
        if (src.length == 0) return 0;
        if (src.length == 1) return src[0];
        double res = op.apply(src[0], src[1]);
        if (src.length == 2) return res;
        for (int i = 2; i < src.length; i++) {
            res = op.apply(res, src[i]);
        }
        return res;
    }

    public static double[] reverse(double[] src, double @Nullable [] res) {
        if (res == null) res = new double[src.length];
        for (int i = 0; i < src.length; i++) {
            res[i] = src[src.length - i - 1];
        }
        return res;
    }

    public static double sum(double[] src) {
        return reduce(src, Double::sum);
    }

    public static double product(double[] src) {
        return reduce(src, (f1, f2) -> f1 * f2);
    }

    public static double arithmeticMean(double[] src) {
        return sum(src) / src.length;
    }

    public static double geometricMean(double[] src) {
        return (double) Math.pow(product(src), 1f / src.length);
    }

    public static double[] concat(double[] a, double[] b) {
        double[] res = new double[a.length + b.length];
        System.arraycopy(a, 0, res, 0, a.length);
        System.arraycopy(b, 0, res, a.length, b.length);
        return res;
    }

    public static double[] flatten(double[]... src) {
        if (src.length == 0) return EMPTY;
        if (src.length == 1) return src[0];
        if (src.length == 2) return concat(src[0], src[1]);
        int n = 0;
        for (double[] doubles : src) n += doubles.length;
        if (n == 0) return EMPTY;
        double[] res = new double[n];
        n = 0;
        for (double[] doubles : src) {
            System.arraycopy(doubles, 0, res, n, doubles.length);
            n += doubles.length;
        }
        return res;
    }

    public interface UnaryDoubleOperator {

        double apply(double v);
    }

    public interface BinaryDoubleOperator {

        double apply(double v, double op);
    }

    public interface TernaryDoubleOperator {

        double apply(double v, double op1, double op2);
    }

    public interface NDoubleOperator {

        double apply(double v, double[] op);
    }
}
