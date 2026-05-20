package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.machine.feature.IVoidable;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.OldRecipeHandlerList;

import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerList;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface IRecipeCapabilityHolder {

//    default boolean hasCapabilityProxies() {
//        return !getCapabilitiesProxy().isEmpty();
//    }
//
//    @NotNull
//    Map<IO, List<OldRecipeHandlerList>> getCapabilitiesProxy();
//
//    @NotNull
//    Map<IO, Map<RecipeCapability<?>, List<IRecipeHandler<?>>>> getCapabilitiesFlat();
//
//    @NotNull
//    default List<OldRecipeHandlerList> getCapabilitiesForIO(IO io) {
//        return getCapabilitiesProxy().getOrDefault(io, Collections.emptyList());
//    }
//
    @NotNull
    default List<IRecipeHandler<?>> getCapabilitiesFlat(IO io, RecipeCapability<?> cap) {
        List<IRecipeHandler<?>> list = new ArrayList<>();
        getRecipeHandlerLists().forEach(h -> list.addAll(h.getAllHandlers()));
        return list.stream().filter(h -> h.getHandlerIO() == io)
                .filter(h -> h.getCapability() == cap)
                .toList();
    }
//
//    default void addHandlerList(OldRecipeHandlerList handlerList) {
//        if (handlerList == OldRecipeHandlerList.NO_DATA) return;
//        IO io = handlerList.getHandlerIO();
//        getCapabilitiesProxy().computeIfAbsent(io, i -> new ArrayList<>()).add(handlerList);
//        var entrySet = handlerList.getHandlerMap().entrySet();
//        var inner = getCapabilitiesFlat().computeIfAbsent(io, i -> new Reference2ObjectOpenHashMap<>(entrySet.size()));
//        for (var entry : entrySet) {
//            var entryList = entry.getValue();
//            inner.computeIfAbsent(entry.getKey(), c -> new ArrayList<>(entryList.size())).addAll(entryList);
//        }
//    }

    @NotNull
    List<RecipeHandlerList> getRecipeHandlerLists();

    default List<RecipeHandlerGroup> getRecipeHandlerGroups() {
        Int2ObjectArrayMap<RecipeHandlerGroup> coloredGroups = new Int2ObjectArrayMap<>();

        List<RecipeHandlerGroup> distinctGroups = new ArrayList<>();
        List<NotifiableRecipeHandlerTrait<?>> byPassHandlers = new ArrayList<>();
        var list = getRecipeHandlerLists();
        list.sort(RecipeHandlerList.COMPARATOR.reversed());
        for(var handlerList : list) {
            if(handlerList.isDistinct()) {
                distinctGroups.add(RecipeHandlerGroup.of(handlerList));
            }
            else if (handlerList.isDyed()) {
                coloredGroups.computeIfAbsent(handlerList.getColor(), c -> new RecipeHandlerGroup())
                        .addHandlerList(handlerList);
            } else {
                for(var group: coloredGroups.values()) {
                    group.addHandlerList(handlerList);
                }
                for(var handler: handlerList.getAllHandlers()) {
                    if(handler.getHandlerIO() == IO.OUT || handler.getCapability().shouldBypassDistinct()){
                        distinctGroups.forEach(g -> g.addHandlers(List.of(handler)));
                    }
                }
            }

        }

        distinctGroups.addAll(coloredGroups.values());
        if(distinctGroups.isEmpty()) {
            var simpleGroup = new RecipeHandlerGroup();
            list.forEach(simpleGroup::addHandlerList);
            distinctGroups = List.of(simpleGroup);
        }
        else {
            distinctGroups.sort(RecipeHandlerGroup.PRIORITY_COMPARATOR);
        }
        if(this instanceof IVoidable voidable) {
            distinctGroups.forEach(g->g.setOutputVoid(voidable::canVoidRecipeOutputs));
        }

        return distinctGroups;

    }

}
