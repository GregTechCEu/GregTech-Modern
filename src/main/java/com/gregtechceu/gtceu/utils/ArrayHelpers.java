package com.gregtechceu.gtceu.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;

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
}
