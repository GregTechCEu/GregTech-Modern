package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface IRecipeCapabilityHolder {

    default boolean hasCapabilityProxies() {
        return !getCapabilitiesProxy().isEmpty();
    }

    @NotNull
    Map<IO, List<RecipeHandlerList>> getCapabilitiesProxy();

    Map<IO, Map<RecipeCapability<?>, List<IRecipeHandler<?>>>> getCapabilitiesFlat();

    default List<IRecipeHandler<?>> getCapabilitiesFlat(IO io, RecipeCapability<?> cap) {
        return getCapabilitiesFlat()
                .getOrDefault(io, Collections.emptyMap())
                .getOrDefault(cap, Collections.emptyList());
    }

    default void addHandlerList(RecipeHandlerList handler) {
        IO io = handler.getHandlerIO();
        getCapabilitiesProxy().computeIfAbsent(io, i -> new ArrayList<>()).add(handler);
        var inner = getCapabilitiesFlat().computeIfAbsent(io, i -> new Reference2ObjectOpenHashMap<>());
        for (var entry : handler.handlerMap.entrySet()) {
            inner.computeIfAbsent(entry.getKey(), c -> new ArrayList<>()).addAll(entry.getValue());
        }
    }
}
