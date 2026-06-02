package com.gregtechceu.gtceu.api.recipe.handler;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class HandlerListMap {
    private final Map<RecipeCapability<?>, List<? extends IRecipeHandler<?>>> handlersMap = new Reference2ObjectArrayMap<>();

    public <T> List<IRecipeHandler<T>> get(RecipeCapability<T> capability) {
        return (List<IRecipeHandler<T>>) handlersMap.get(capability);
    }

    public <T> void add(RecipeCapability<T> capability, IRecipeHandler<T> handler) {
        ((List<IRecipeHandler<T>>) handlersMap.computeIfAbsent(capability, c -> new ArrayList<>()))
                .add(handler);
    }

    public <T> void add(IRecipeHandler<T> handler) {
        ((List<IRecipeHandler<T>>) handlersMap.computeIfAbsent(handler.getCapability(), c -> new ArrayList<>()))
                .add(handler);
    }

    public <T> List<RecipeCapability<T>> getOrDefault(RecipeCapability<T> capability, List<IRecipeHandler<T>> fallback) {
        return (List<RecipeCapability<T>>) handlersMap.getOrDefault(capability, fallback);
    }

    public boolean isEmpty() {
        return handlersMap.isEmpty();
    }

    public Collection<List<? extends IRecipeHandler<?>>> values() {
        return handlersMap.values();
    }
}
