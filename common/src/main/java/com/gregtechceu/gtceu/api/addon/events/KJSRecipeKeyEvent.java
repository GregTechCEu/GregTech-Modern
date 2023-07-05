package com.gregtechceu.gtceu.api.addon.events;

import com.gregtechceu.gtceu.integration.kjs.recipe.components.GTRecipeComponents;
import dev.latvian.mods.kubejs.recipe.RecipeKey;

import java.util.*;

/**
 * @author screret
 * @date 2023/7/5
 * @implNote An event for adding KJS recipe keys
 */
public class KJSRecipeKeyEvent {
    private final Set<RecipeKey<Map<String, Object>>> registeredKeys = new HashSet<>();

    /**
     * Use this to register new components for KJS to use!
     * <br/>
     * has to sadly be separate, because you need to use RecipeComponentBuilder to build an object for this
     * <br/>
     * see {@link GTRecipeComponents#ITEM} and {@link GTRecipeComponents#ALL_ANY}
     */
    public void registerKey(RecipeKey<Map<String, Object>> key) {
        if (registeredKeys.add(key)) {
            throw new IllegalStateException("Can't have multiple Recipe keys with same value!");
        }
    }

    @SuppressWarnings("unchecked")
    public Set<RecipeKey<Map<String, Object>>> getRegisteredKeys() {
        return Set.of(registeredKeys.toArray(RecipeKey[]::new));
    }

}
