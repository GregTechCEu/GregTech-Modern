package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RecipeHandlerList {

    public static RecipeHandlerList NO_DATA = new RecipeHandlerList(IO.NONE);

    public static final Comparator<RecipeHandlerList> COMPARATOR = (h1, h2) -> {
        int cmp = Long.compare(h1.getPriority(), h2.getPriority());
        if (cmp != 0) return cmp;
        boolean b1 = h1.getTotalContentAmount() > 0;
        boolean b2 = h2.getTotalContentAmount() > 0;
        return Boolean.compare(b1, b2);
    };

    public final Map<RecipeCapability<?>, List<IRecipeHandler<?>>> handlerMap = new Reference2ObjectOpenHashMap<>();
    private final List<IRecipeHandler<?>> allHandlers = new ObjectArrayList<>();
    private final IO io;
    @Setter
    @Getter
    private boolean isDistinct = false;
    private long priority = 0;

    public RecipeHandlerList(IO io) {
        this.io = io;
    }

    public static RecipeHandlerList of(IO io, IRecipeHandler<?> handler) {
        RecipeHandlerList rhl = new RecipeHandlerList(io);
        rhl.addHandlers(handler);
        return rhl;
    }

    public List<IRecipeHandler<?>> getCapability(RecipeCapability<?> cap) {
        return handlerMap.getOrDefault(cap, Collections.emptyList());
    }

    public void addHandlers(IRecipeHandler<?>... handlers) {
        addHandlers(Arrays.asList(handlers));
    }

    public void addHandlers(List<IRecipeHandler<?>> handlers) {
        for (var handler : handlers) {
            handlerMap.computeIfAbsent(handler.getCapability(), c -> new ArrayList<>()).add(handler);
            priority += handler.getPriority();
        }
        allHandlers.addAll(handlers);
        sort();
    }

    public void sort() {
        for (var list : handlerMap.values()) {
            list.sort(IRecipeHandler.ENTRY_COMPARATOR);
        }
    }

    public boolean hasCapability(RecipeCapability<?> cap) {
        return handlerMap.containsKey(cap);
    }

    public IO getHandlerIO() {
        return io;
    }

    public long getPriority() {
        long priority = 0;
        for (var handler : allHandlers) priority += handler.getPriority();
        return priority;
    }

    public double getTotalContentAmount() {
        double sum = 0;
        for (var handler : allHandlers) sum += handler.getTotalContentAmount();
        return sum;
    }

    public List<ISubscription> addChangeListeners(Runnable listener) {
        List<ISubscription> ret = new ArrayList<>();
        for (var handlerList : handlerMap.values()) {
            for (var handler : handlerList) {
                if (handler instanceof IRecipeHandlerTrait<?> handlerTrait) {
                    ret.add(handlerTrait.addChangedListener(listener));
                }
            }
        }
        return ret;
    }

    public Map<RecipeCapability<?>, List> handleRecipe(IO io, GTRecipe recipe, Map<RecipeCapability<?>, List> contents,
                                                       boolean simulate) {
        if (handlerMap.isEmpty()) return contents;
        var copy = new Reference2ObjectOpenHashMap<>(contents);
        var it = copy.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            var handlerList = handlerMap.get(entry.getKey());
            if (handlerList == null)
                continue;
            for (var handler : handlerList) {
                var left = handler.handleRecipe(io, recipe, entry.getValue(), simulate);
                if (left == null) {
                    it.remove();
                    break;
                } else {
                    entry.setValue(left);
                }
            }
        }
        return copy;
    }
}
