package com.gregtechceu.gtceu.utils;

import java.lang.reflect.Field;

public class ReflectionUtils {

    public static <O, I> O getOuterClassObject(I innerClass, Class<O> outerClassType) {
        try {
            for (Field field : innerClass.getClass().getDeclaredFields()) {
                if (field.getType().isAssignableFrom(outerClassType)) {
                    field.setAccessible(true);
                    return (O) field.get(innerClass);
                }
            }
        } catch (IllegalAccessException ignored) {}
        return null;
    }
}
