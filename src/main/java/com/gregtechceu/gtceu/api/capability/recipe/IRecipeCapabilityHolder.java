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

    @NotNull
    Map<IO, Map<RecipeCapability<?>, List<IRecipeHandler<?>>>> getCapabilitiesFlat();

    @NotNull
    default List<RecipeHandlerList> getCapabilitiesForIO(IO io) {
        return getCapabilitiesProxy().getOrDefault(io, Collections.emptyList());
    }

    @NotNull
    default List<IRecipeHandler<?>> getCapabilitiesFlat(IO io, RecipeCapability<?> cap) {
        return getCapabilitiesFlat()
                .getOrDefault(io, Collections.emptyMap())
                .getOrDefault(cap, Collections.emptyList());
    }

    default void addHandlerList(RecipeHandlerList handlerList) {
        if (handlerList == RecipeHandlerList.NO_DATA) return;
        IO io = handlerList.getHandlerIO();
        getCapabilitiesProxy().computeIfAbsent(io, i -> new ArrayList<>()).add(handlerList);
        var entrySet = handlerList.getHandlerMap().entrySet();
        var inner = getCapabilitiesFlat().computeIfAbsent(io, i -> new Reference2ObjectOpenHashMap<>(entrySet.size()));
        for (var entry : entrySet) {
            var entryList = entry.getValue();
            inner.computeIfAbsent(entry.getKey(), c -> new ArrayList<>(entryList.size())).addAll(entryList);
        }
    }
}
