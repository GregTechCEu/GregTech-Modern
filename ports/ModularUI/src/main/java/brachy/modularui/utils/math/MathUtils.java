package brachy.modularui.utils.math;

import org.jspecify.annotations.NullMarked;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import com.ezylang.evalex.BaseException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;

@NullMarked
@ParametersAreNonnullByDefault
public class MathUtils {

    public static final float PI_QUART = Mth.PI / 4f;

    public static final ExpressionConfiguration MATH_CFG = ExpressionConfiguration.builder()
            .arraysAllowed(false)
            .structuresAllowed(false)
            .stripTrailingZeros(true)
            .build()
            .withAdditionalOperators(Pair.of("%", new PostfixPercentOperator()));

    public static ParseResult parseExpression(@Nullable String expression) {
        return parseExpression(expression, true);
    }

    public static ParseResult parseExpression(@Nullable String expression, boolean useSiPrefixes) {
        return parseExpression(expression, Double.NaN, useSiPrefixes);
    }

    public static ParseResult parseExpression(@Nullable String expression, double defaultValue) {
        return parseExpression(expression, defaultValue, true);
    }

    public static ParseResult parseExpression(@Nullable String expression, double defaultValue, boolean useSiPrefixes) {
        if (expression == null || expression.isEmpty()) {
            return ParseResult.success(EvaluationValue.numberValue(new BigDecimal(defaultValue)));
        }

        Expression e = new Expression(expression, MATH_CFG);
        if (useSiPrefixes) {
            SIPrefix.addAllToExpression(e);
        }
        try {
            return ParseResult.success(e.evaluate());
        } catch (BaseException exception) {
            return ParseResult.failure(exception);
        }
    }

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
            int intValue = (int) Math.min(value, Integer.MAX_VALUE);
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

    public static int min(int @Nullable ... values) {
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

    public static int max(int @Nullable ... values) {
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

    public static float arithmeticGeometricMean(float a, float b) {
        return arithmeticGeometricMean(a, b, 5);
    }

    public static float arithmeticGeometricMean(float a, float b, int iterations) {
        a = (a + b) / 2;
        b = Mth.sqrt(a * b);
        if (--iterations == 0) return a;
        return arithmeticGeometricMean(a, b, iterations);
    }

    public static double rescaleLinear(double v, double fromMin, double fromMax, double toMin, double toMax) {
        v = (v - fromMin) / (fromMax - fromMin); // reverse lerp
        return toMin + (toMax - toMin) * v; // forward lerp
    }

    public static float rescaleLinear(float v, float fromMin, float fromMax, float toMin, float toMax) {
        v = (v - fromMin) / (fromMax - fromMin); // reverse lerp
        return toMin + (toMax - toMin) * v; // forward lerp
    }

    public static int intPlaces(BigDecimal x) {
        return Math.max(1, x.precision() - x.scale());
    }

    public static int intPlaces(double x) {
        if (x == 0.0) return 1;
        x = Math.abs(x);
        int d = (int) Math.floor(Math.log10(x)) + 1;
        // correct rounding errors
        if (Math.pow(10, d - 1) > x) d--;
        return Math.max(d, 1);
    }
}
