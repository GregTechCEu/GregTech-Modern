package com.gregtechceu.gtceu.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Objects;

import static org.apache.commons.lang3.ArrayUtils.*;

@SuppressWarnings("ForLoopReplaceableByForEach")
public class ArrayHelpers {

    /**
     * Returns a copy of the specified array object, deeply copying multidimensional arrays.
     * If the specified object is null, the return value is null.
     *
     * <p>
     * Note: if the array object has an element type which is a reference type that is not an array type,
     * the elements themselves are not deeply copied. This method only copies array objects.
     *
     * @param array the array object to deep copy
     * @param <T>   the type of the array to deep copy
     * @return a copy of the specified array object, deeply copying multidimensional arrays, or null if the object is
     *         null
     */
    @Contract(value = "!null -> !null; _ -> null", pure = true)
    public static <T> T @Nullable [] deepCopy(T @Nullable [] array) {
        if (array == null) {
            return null;
        }

        Class<?> componentType = array.getClass().getComponentType();

        @SuppressWarnings("unchecked")
        T[] copy = (T[]) Array.newInstance(componentType, array.length);

        if (componentType.isArray()) {
            for (int i = 0; i < array.length; ++i) {
                // noinspection unchecked
                Array.set(copy, i, deepCopy((T[]) array[i]));
            }
        } else {
            System.arraycopy(array, 0, copy, 0, array.length);
        }

        return copy;
    }

    public static boolean allMatch(boolean[] array) {
        if (isEmpty(array) || array.length == 1) {
            return true;
        }

        boolean firstElement = array[0];
        if (array.length == 2) return firstElement == array[1];

        for (int i = 0; i < array.length; ++i) {
            if (array[i] != firstElement) {
                return false;
            }
        }
        return true;
    }

    public static boolean allMatch(byte[] array) {
        if (isEmpty(array) || array.length == 1) {
            return true;
        }

        byte firstElement = array[0];
        if (array.length == 2) return firstElement == array[1];

        for (int i = 0; i < array.length; ++i) {
            if (array[i] != firstElement) {
                return false;
            }
        }
        return true;
    }

    public static boolean allMatch(char[] array) {
        if (isEmpty(array) || array.length == 1) {
            return true;
        }

        char firstElement = array[0];
        if (array.length == 2) return firstElement == array[1];

        for (int i = 0; i < array.length; ++i) {
            if (array[i] != firstElement) {
                return false;
            }
        }
        return true;
    }

    public static boolean allMatch(double[] array) {
        if (isEmpty(array) || array.length == 1) {
            return true;
        }

        double firstElement = array[0];
        boolean searchNaN = Double.isNaN(firstElement);

        if (array.length == 2) return array[1] == firstElement || searchNaN && Double.isNaN(array[1]);

        for (int i = 0; i < array.length; ++i) {
            if (array[i] != firstElement && !(searchNaN && Double.isNaN(array[i]))) {
                return false;
            }
        }

        return false;
    }

    public static boolean allMatch(double[] array, double tolerance) {
        if (isEmpty(array) || array.length == 1) {
            return true;
        }

        double firstElement = array[0];
        double min = firstElement - tolerance;
        double max = firstElement + tolerance;

        if (array.length == 2) return array[1] >= min && array[1] <= max;

        for (int i = 0; i < array.length; ++i) {
            if (array[i] < min || array[i] > max) {
                return false;
            }
        }
        return true;
    }

    public static boolean allMatch(float[] array) {
        if (isEmpty(array) || array.length == 1) {
            return true;
        }

        float firstElement = array[0];
        boolean searchNaN = Float.isNaN(firstElement);

        if (array.length == 2) return array[1] == firstElement || searchNaN && Float.isNaN(array[1]);

        for (int i = 0; i < array.length; ++i) {
            if (array[i] != firstElement && !(searchNaN && Float.isNaN(array[i]))) {
                return false;
            }
        }

        return false;
    }

    public static boolean allMatch(float[] array, float tolerance) {
        if (isEmpty(array) || array.length == 1) {
            return true;
        }

        float firstElement = array[0];
        float min = firstElement - tolerance;
        float max = firstElement + tolerance;

        if (array.length == 2) return array[1] >= min && array[1] <= max;

        for (int i = 0; i < array.length; ++i) {
            if (array[i] < min && array[i] > max) {
                return false;
            }
        }
        return true;
    }

    public static boolean allMatch(int[] array) {
        if (isEmpty(array) || array.length == 1) {
            return true;
        }

        int firstElement = array[0];
        if (array.length == 2) return firstElement == array[1];

        for (int i = 0; i < array.length; ++i) {
            if (array[i] != firstElement) {
                return false;
            }
        }
        return true;
    }

    public static boolean allMatch(long[] array) {
        if (isEmpty(array) || array.length == 1) {
            return true;
        }

        long firstElement = array[0];
        if (array.length == 2) return firstElement == array[1];

        for (int i = 0; i < array.length; ++i) {
            if (array[i] != firstElement) {
                return false;
            }
        }
        return true;
    }

    public static boolean allMatch(Object[] array) {
        if (isEmpty(array) || array.length == 1) {
            return true;
        }

        Object firstElement = array[0];
        if (array.length == 2) return Objects.equals(array[1], firstElement);

        for (int i = 0; i < array.length; ++i) {
            if (!Objects.equals(array[i], firstElement)) {
                return false;
            }
        }
        return true;
    }

    public static boolean allMatch(short[] array) {
        if (isEmpty(array) || array.length == 1) {
            return true;
        }

        short firstElement = array[0];
        if (array.length == 2) return firstElement == array[1];

        for (int i = 0; i < array.length; ++i) {
            if (array[i] != firstElement) {
                return false;
            }
        }
        return true;
    }
}
