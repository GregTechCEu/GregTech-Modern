package com.gregtechceu.gtceu.api.addon.events;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.ContentJS;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.GTRecipeComponents;

import com.mojang.datafixers.util.Pair;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class KJSRecipeKeyEvent {

    @Getter
    private final Map<RecipeCapability<?>, Pair<ContentJS<?>, ContentJS<?>>> registeredKeys = new HashMap<>();

    /**
     * Use this to register new components for KJS to use!
     * 
     * @param cap the recipe capability you're adding a KJS binding for.
     * @param key the components, like {@link GTRecipeComponents#ITEM_IN} and {@link GTRecipeComponents#ITEM_OUT}, as a
     *            {@link Pair}
     */
    public void registerKey(RecipeCapability<?> cap, Pair<ContentJS<?>, ContentJS<?>> key) {
        if (registeredKeys.put(cap, key) != null) {
            throw new IllegalStateException("Can't have multiple Recipe keys with same value!");
        }
    }
}
