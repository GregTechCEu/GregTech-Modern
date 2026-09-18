package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.utils.ISubscription;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    RecipeCapabilityVersions getRecipeVersions();

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

    @Nullable
    default ISubscription addHandlerList(RecipeHandlerList handlerList) {
        if (handlerList == RecipeHandlerList.NO_DATA) return null;
        IO io = handlerList.getHandlerIO();
        getCapabilitiesProxy().computeIfAbsent(io, i -> new ArrayList<>()).add(handlerList);
        var entrySet = handlerList.getHandlerMap().entrySet();
        var inner = getCapabilitiesFlat().computeIfAbsent(io, i -> new Reference2ObjectOpenHashMap<>(entrySet.size()));
        for (var entry : entrySet) {
            var entryList = entry.getValue();
            inner.computeIfAbsent(entry.getKey(), c -> new ArrayList<>(entryList.size())).addAll(entryList);
        }

        RecipeCapabilityVersions versions = getRecipeVersions();
        handlerList.attachVersions(versions);
        versions.topologyVersion++;
        if (io.support(IO.IN)) {
            versions.inputVersion++;
        }
        if (io.support(IO.OUT)) {
            versions.outputVersion++;
        }
        return handlerList.subscribe(() -> {
            if (io.support(IO.IN)) {
                versions.inputVersion++;
            }
            if (io.support(IO.OUT)) {
                versions.outputVersion++;
            }
        });
    }
}
