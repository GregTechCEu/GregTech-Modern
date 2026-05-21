package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.machine.feature.IVoidable;

import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerList;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface IRecipeCapabilityHolder {

    @NotNull
    default List<IRecipeHandler<?>> getCapabilitiesFlat(IO io, RecipeCapability<?> cap) {
        List<IRecipeHandler<?>> list = new ArrayList<>();
        getRecipeHandlerLists().forEach(h -> list.addAll(h.getAllHandlers()));
        return list.stream().filter(h -> h.getHandlerIO() == io)
                .filter(h -> h.getCapability() == cap)
                .toList();
    }

    @NotNull
    List<RecipeHandlerList> getRecipeHandlerLists();

    default List<RecipeHandlerGroup> getRecipeHandlerGroups() {
        Int2ObjectArrayMap<RecipeHandlerGroup> coloredGroups = new Int2ObjectArrayMap<>();

        List<RecipeHandlerGroup> distinctGroups = new ArrayList<>();
        var list = new ArrayList<>(getRecipeHandlerLists()) ;
        list.sort(RecipeHandlerList.COMPARATOR.reversed());
        for(var handlerList : list) {
            if(handlerList.isDistinct()) {
                distinctGroups.add(RecipeHandlerGroup.of(handlerList));
            }
            else if (handlerList.isDyed()) {
                coloredGroups.computeIfAbsent(handlerList.getColor(), RecipeHandlerGroup::of)
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
