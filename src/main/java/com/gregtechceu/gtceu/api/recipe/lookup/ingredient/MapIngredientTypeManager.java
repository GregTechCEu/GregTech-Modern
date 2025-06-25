package com.gregtechceu.gtceu.api.recipe.lookup.ingredient;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.CustomMapIngredient;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

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
    private static final Map<Class<?>, List<MapIngredientFunction<?>>> ingredientTypes = new ConcurrentHashMap<>(7);
    // spotless:on

    public static <T> void registerMapIngredient(Class<T> ingredientClass,
                                                 MapIngredientFunction<T> function) {
        ingredientTypes.computeIfAbsent(ingredientClass, $ -> new ArrayList<>()).add(function);
    }

    public static <T> List<AbstractMapIngredient> getFrom(T object) {
        List<AbstractMapIngredient> values = new ArrayList<>();

        var types = getTypesForClass((Class<? super T>) object.getClass());
        for (var function : types) {
            values.addAll(function.getIngredients(object));
            if (function.terminal()) break;
        }
        return values;
    }

    private static <T> List<? extends MapIngredientFunction<? super T>> getTypesForClass(Class<T> clazz) {
        var types = ingredientTypes.get(clazz);
        if (types == null) {
            if (clazz.getSuperclass() == null) return Collections.emptyList();
            return getTypesForClass(clazz.getSuperclass());
        }
        return (List<? extends MapIngredientFunction<T>>) types;
    }
}
