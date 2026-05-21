package com.gregtechceu.gtceu.api.recipe.handler;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class RecipeHandlerGroup {

    public static final Comparator<RecipeHandlerGroup> PRIORITY_COMPARATOR =
            (g1, g2) ->
            g1.getPriority(IO.IN) - g2.getPriority(IO.IN);


    public static final RecipeHandlerGroup EMPTY = new RecipeHandlerGroup();

    @Getter
    private final Map<RecipeCapability<?>, List<IRecipeHandler<?>>> inputHandlerMap = new Reference2ObjectOpenHashMap<>();

    @Getter
    private final Map<RecipeCapability<?>, List<IRecipeHandler<?>>> outputHandlerMap = new Reference2ObjectOpenHashMap<>();

    @Getter
    @Setter
    @NotNull
    private Predicate<RecipeCapability<?>> outputVoid = cap -> false;

    @Getter
    @Setter
    private int color = RecipeHandlerList.UNDYED;

    public static RecipeHandlerGroup of(int color) {
        var group = new RecipeHandlerGroup();
        group.color = color;
        return group;
    }

    public static RecipeHandlerGroup of(RecipeHandlerList handlerList) {
        var group = new RecipeHandlerGroup();
        group.addHandlerList(handlerList);
        return group;
    }

    public void addHandlers(Collection<IRecipeHandler<?>> handlers) {
        for(var handler: handlers) {
            if(handler.getHandlerIO().support(IO.IN)) {
                inputHandlerMap.computeIfAbsent(handler.getCapability(), c -> new ArrayList<>())
                        .add(handler);
            }
            if(handler.getHandlerIO().support(IO.OUT)) {
                outputHandlerMap.computeIfAbsent(handler.getCapability(), c -> new ArrayList<>())
                        .add(handler);
            }
        }
    }

    public void addHandlerList(RecipeHandlerList handlerList) {
        addHandlers(handlerList.getAllHandlers());
    }

    public Map<RecipeCapability<?>, List<Object>> handleRecipe(IO io, GTRecipe recipe,
                                                               Map<RecipeCapability<?>, List<Object>> contents,
                                                               boolean simulate) {
        var copy = new Reference2ObjectOpenHashMap<>(contents);
        for (var it = copy.reference2ObjectEntrySet().fastIterator(); it.hasNext();) {
            var entry = it.next();
            var handlerList = getCapability(io, entry.getKey());
            if(handlerList == null) continue;
            for (var handler : handlerList) {
                var left = handler.handleRecipe(io, recipe, entry.getValue(), simulate);
                if (left == null) {
                    it.remove();
                    break;
                } else {
                    entry.setValue(new ArrayList<>(left));
                }
            }
        }
        return copy;
    }

    private @Nullable List<IRecipeHandler<?>> getCapability(IO io, RecipeCapability<?> cap) {
        if(io == IO.IN) {
            return inputHandlerMap.get(cap);
        }
        else if (io == IO.OUT){
            return outputHandlerMap.get(cap);
        }
        else {
            throw new RuntimeException("Error IO Type");
        }
    }

    List<IRecipeHandler<?>> getCapabilitiesFalt(IO io) {
        List<IRecipeHandler<?>> list = new ArrayList<>();
        if(io == IO.IN) {
            for (var handlers : inputHandlerMap.values()) list.addAll(handlers);
        }
        else if (io == IO.OUT){
            for (var handlers : outputHandlerMap.values()) list.addAll(handlers);
        }
        else {
            throw new RuntimeException("Error IO Type");
        }
        return list;
    }

    public int getPriority(IO io) {
        int priority = 0;
        for(var handler: getCapabilitiesFalt(io)) priority+=handler.getPriority();
        return priority;
    }

    public boolean isEmpty() {
        return this == EMPTY || (inputHandlerMap.isEmpty() && outputHandlerMap.isEmpty());
    }
}
