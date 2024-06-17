package com.gregtechceu.gtceu.api.addon.events;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.ContentJS;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.GTRecipeComponents;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author screret
 * @date 2023/7/5
 * @implNote An event for adding KJS recipe keys
 */
@SuppressWarnings("unused")
public class KJSRecipeKeyEvent {

    @Getter
    private final Map<RecipeCapability<?>, ContentJS<?>> registeredKeys = new HashMap<>();

    /**
     * Use this to register new components for KJS to use!
     * 
     * @param cap the recipe capability you're adding a KJS binding for.
     * @param key the component, like {@link GTRecipeComponents#ITEM}
     */
    public void registerKey(RecipeCapability<?> cap, ContentJS<?> key) {
        if (registeredKeys.put(cap, key) != null) {
            throw new IllegalStateException("Can't have multiple Recipe keys with same value!");
        }
    }
}
