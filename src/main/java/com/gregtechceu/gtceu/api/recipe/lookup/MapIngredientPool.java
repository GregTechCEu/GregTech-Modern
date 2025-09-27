package com.gregtechceu.gtceu.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Pool for {@link AbstractMapIngredient} to save memory
 */
@ApiStatus.Internal
public final class MapIngredientPool {

    private static final Map<AbstractMapIngredient, WeakReference<AbstractMapIngredient>> pool = new WeakHashMap<>();

    /**
     * Replaces values in a list of ingredients with pooled versions,
     * and pools the existing ingredients if not already pooled.
     *
     * @param list the list
     */
    static void applyPooling(@NotNull List<AbstractMapIngredient> list) {
        for (int i = 0; i < list.size(); i++) {
            AbstractMapIngredient ingredient = list.get(i);
            var pooledReference = pool.get(ingredient);
            if (pooledReference == null) {
                pool.put(ingredient, new WeakReference<>(ingredient));
                continue;
            }
            var pooled = pooledReference.get();
            if (pooled == null) {
                pool.put(ingredient, new WeakReference<>(ingredient));
            } else {
                list.set(i, pooled);
            }
        }
    }

    /**
     * Clear the ingredient pool
     */
    public static void clear() {
        pool.clear();
    }
}
