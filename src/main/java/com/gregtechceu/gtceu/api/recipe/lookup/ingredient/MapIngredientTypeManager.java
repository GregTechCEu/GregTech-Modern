package com.gregtechceu.gtceu.api.recipe.lookup.ingredient;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;

import net.minecraft.Util;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager for custom map ingredient types.
 * <br>
 * Addons can register their own map ingredient classes here instead of mixining into GT's initialization logic.
 * <p>
 * A good time to register them is in a {@link FMLCommonSetupEvent Common Setup Event},
 * after any custom ingredient types.
 */
@SuppressWarnings("unchecked")
public final class MapIngredientTypeManager {

    // spotless:off
    private static final Map<Class<?>, List<? extends MapIngredientFunction<?>>> ingredientFunctions = new ConcurrentHashMap<>(7);
    private static final Map<MapIngredientFunction<?>, Class<?>> ingredientTypes = new ConcurrentHashMap<>(7);
    // spotless:on

    public static <T> void registerMapIngredient(Class<T> ingredientClass,
                                                 MapIngredientFunction<T> function) {
        ingredientClass = boxClass(ingredientClass);
        var list = (List<MapIngredientFunction<T>>) ingredientFunctions.computeIfAbsent(
                ingredientClass, $ -> new ArrayList<>());
        list.add(function);
        ingredientTypes.put(function, ingredientClass);
    }

    @NotNull
    public static <T> List<AbstractMapIngredient> getFrom(T object, RecipeCapability<?> cap) {
        Class<? super T> objClass = (Class<? super T>) boxClass(object.getClass());
        Class<?> stopAt = boxClass(cap.serializer.contentClass());
        if (!stopAt.isAssignableFrom(objClass)) {
            stopAt = Object.class;
        }
        var functions = getTypesForClass(objClass, stopAt);
        // this is the same as writing `object instanceof stopClass`, but it keeps track of the boxed primitives
        if (!objClass.isAssignableFrom(stopAt)) {
            var defaults = getDefaultIngredients(object, cap, stopAt, functions);
            if (defaults != null) return defaults;
        }

        List<AbstractMapIngredient> values = new ArrayList<>();
        for (var function : functions) {
            values.addAll(function.getIngredients(object));
        }
        return values;
    }

    private static <T> List<? extends MapIngredientFunction<? super T>> getTypesForClass(Class<T> objClass,
                                                                                         Class<?> stopAt) {
        Preconditions.checkArgument(stopAt.isAssignableFrom(objClass),
                "stopAt must be a superclass of %s", objClass);

        var types = ingredientFunctions.get(objClass);
        if (types == null && objClass != stopAt) {
            Class<? super T> superclass = objClass.getSuperclass();
            if (superclass == null || superclass == stopAt) return Collections.emptyList();
            return getTypesForClass(superclass, stopAt);
        }
        return (List<MapIngredientFunction<T>>) types;
    }

    private static <T> @Nullable List<AbstractMapIngredient> getDefaultIngredients(T object, RecipeCapability<?> cap,
                                                                                   Class<?> stopAt,
                                                                                   List<? extends MapIngredientFunction<? super T>> functions) {
        for (var function : functions) {
            if (ingredientTypes.get(function) != stopAt) {
                return null;
            }
        }
        // if the ingredient is not of the base type, and we didn't find any specific ones for it, return a default
        return Objects.requireNonNullElseGet(cap.getDefaultMapIngredient(object), Collections::emptyList);
    }

    private static final Map<Class<?>, Class<?>> WRAPPERS = Util.make(new HashMap<>(9), map -> {
        map.put(boolean.class, Boolean.class);
        map.put(byte.class, Byte.class);
        map.put(char.class, Character.class);
        map.put(double.class, Double.class);
        map.put(float.class, Float.class);
        map.put(int.class, Integer.class);
        map.put(long.class, Long.class);
        map.put(short.class, Short.class);
        map.put(void.class, Void.class);
    });

    private static <T> @NotNull Class<T> boxClass(Class<T> clazz) {
        return (Class<T>) WRAPPERS.getOrDefault(clazz, clazz);
    }
}
