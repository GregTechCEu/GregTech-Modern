package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.utils.math.ParseResult;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.mariuszgromada.math.mxparser.Constant;
import org.mariuszgromada.math.mxparser.Expression;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTMath {

    // SI prefixes
    public static final Constant k = new Constant("k", 1e3);
    public static final Constant M = new Constant("M", 1e6);
    public static final Constant G = new Constant("G", 1e9);
    public static final Constant T = new Constant("T", 1e12);
    public static final Constant P = new Constant("P", 1e15);
    public static final Constant E = new Constant("E", 1e18);
    public static final Constant Z = new Constant("Z", 1e21);
    public static final Constant Y = new Constant("Y", 1e24);
    public static final Constant m = new Constant("m", 1e-3);
    public static final Constant u = new Constant("u", 1e-6);
    public static final Constant n = new Constant("n", 1e-9);
    public static final Constant p = new Constant("p", 1e-12);
    public static final Constant f = new Constant("f", 1e-15);
    public static final Constant a = new Constant("a", 1e-18);
    public static final Constant z = new Constant("z", 1e-21);
    public static final Constant y = new Constant("y", 1e-24);

    public static final float QUART_PI = Mth.PI / 4f;

    public static final Vector3fc UNIT_X = new Vector3f(1f, 0f, 0f);
    public static final Vector3fc UNIT_Y = new Vector3f(0f, 1f, 0f);
    public static final Vector3fc UNIT_Z = new Vector3f(0f, 0f, 1f);

    public static int lerpInt(double delta, int start, int end) {
        return start + Mth.floor(delta * (end - start));
    }

    public static List<ItemStack> splitStacks(ItemStack stack, long amount) {
        int fullStacks = (int) (amount / Integer.MAX_VALUE);
        int rem = (int) (amount % Integer.MAX_VALUE);
        List<ItemStack> stacks = new ObjectArrayList<>(fullStacks + 1);
        if (fullStacks > 0) stacks.addAll(Collections.nCopies(fullStacks, stack.copyWithCount(Integer.MAX_VALUE)));
        if (rem > 0) stacks.add(stack.copyWithCount(rem));
        return stacks;
    }

    public static List<FluidStack> splitFluidStacks(FluidStack stack, long amount) {
        int fullStacks = (int) (amount / Integer.MAX_VALUE);
        int rem = (int) (amount % Integer.MAX_VALUE);
        List<FluidStack> stacks = new ObjectArrayList<>(fullStacks + 1);
        if (fullStacks > 0) {
            var copy = stack.copy();
            copy.setAmount(Integer.MAX_VALUE);
            stacks.addAll(Collections.nCopies(fullStacks, copy));
        }
        if (rem > 0) {
            var copy = stack.copy();
            copy.setAmount(rem);
            stacks.add(copy);
        }
        return stacks;
    }

    public static int[] split(long value) {
        IntArrayList result = new IntArrayList();
        while (value > 0) {
            int intValue = (int) java.lang.Math.min(value, Integer.MAX_VALUE);
            result.add(intValue);
            value -= intValue;
        }
        return result.toIntArray();
    }

    public static int saturatedCast(long value) {
        if (value > 2147483647L) {
            return Integer.MAX_VALUE;
        } else {
            return value < -2147483648L ? Integer.MIN_VALUE : (int) value;
        }
    }

    public static int hashInts(int... vals) {
        return Arrays.hashCode(vals);
    }

    public static int hashLongs(long... vals) {
        return Arrays.hashCode(vals);
    }

    public static float ratio(BigInteger a, BigInteger b) {
        return new BigDecimal(a).divide(new BigDecimal(b), MathContext.DECIMAL32).floatValue();
    }

    public static int ceilDiv(int x, int y) {
        final int q = x / y;
        // if the signs are the same and modulo not zero, round up
        if ((x ^ y) >= 0 && (q * y != x)) {
            return q + 1;
        }
        return q;
    }

    public static ParseResult parseExpression(@Nullable String expression) {
        return parseExpression(expression, Double.NaN, false);
    }

    public static ParseResult parseExpression(@Nullable String expression, boolean useSiPrefixes) {
        return parseExpression(expression, Double.NaN, useSiPrefixes);
    }

    public static ParseResult parseExpression(@Nullable String expression, double defaultValue) {
        return parseExpression(expression, defaultValue, true);
    }

    public static ParseResult parseExpression(@Nullable String expression, double defaultValue, boolean useSiPrefixes) {
        if (expression == null || expression.isEmpty()) return ParseResult.success(defaultValue);
        Expression e = new Expression(expression);
        if (useSiPrefixes) {
            e.addConstants(k, M, G, T, P, E, Z, Y, m, u, n, p, f, a, z, y);
        }
        double result = e.calculate();
        if (Double.isNaN(result)) {
            return ParseResult.failure(defaultValue, e.getErrorMessage());
        }
        return ParseResult.success(result);
    }

    public static long clamp(long v, long min, long max) {
        return Math.max(min, Math.min(max, v));
    }

    public static int cycler(int x, int min, int max) {
        return x < min ? max : (x > max ? min : x);
    }

    public static float cycler(float x, float min, float max) {
        return x < min ? max : (x > max ? min : x);
    }

    public static double cycler(double x, double min, double max) {
        return x < min ? max : (x > max ? min : x);
    }

    public static int gridIndex(int x, int y, int size, int width) {
        x = x / size;
        y = y / size;

        return x + y * width / size;
    }

    public static int gridRows(int count, int size, int width) {
        double x = count * size / (double) width;

        return count <= 0 ? 1 : (int) Math.ceil(x);
    }

    public static int min(int @Nullable... values) {
        if (values == null || values.length == 0) throw new IllegalArgumentException();
        if (values.length == 1) return values[0];
        if (values.length == 2) return Math.min(values[0], values[1]);
        int min = Integer.MAX_VALUE;
        for (int i : values) {
            if (i < min) {
                min = i;
            }
        }
        return min;
    }

    public static int max(int @Nullable... values) {
        if (values == null || values.length == 0) throw new IllegalArgumentException();
        if (values.length == 1) return values[0];
        if (values.length == 2) return Math.max(values[0], values[1]);
        int max = Integer.MIN_VALUE;
        for (int i : values) {
            if (i > max) {
                max = i;
            }
        }
        return max;
    }
}
