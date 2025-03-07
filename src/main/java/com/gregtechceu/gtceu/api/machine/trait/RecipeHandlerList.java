package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RecipeHandlerList {

    public static final RecipeHandlerList NO_DATA = new RecipeHandlerList(IO.NONE);

    public static final Comparator<RecipeHandlerList> COMPARATOR = (h1, h2) -> {
        int cmp = Long.compare(h1.getPriority(), h2.getPriority());
        if (cmp != 0) return cmp;
        boolean b1 = h1.getTotalContentAmount() > 0;
        boolean b2 = h2.getTotalContentAmount() > 0;
        return Boolean.compare(b1, b2);
    };

    @Getter
    private final Map<RecipeCapability<?>, List<IRecipeHandler<?>>> handlerMap = new Reference2ObjectOpenHashMap<>();
    private final List<IRecipeHandler<?>> allHandlers = new ArrayList<>();
    private final List<IRecipeHandlerTrait<?>> allHandlerTraits = new ArrayList<>();

    @Getter
    private final IO handlerIO;
    @Getter
    private boolean isDistinct = false;

    public RecipeHandlerList(IO handlerIO) {
        this.handlerIO = handlerIO;
    }

    public static RecipeHandlerList of(IO io, IRecipeHandler<?> handler) {
        RecipeHandlerList rhl = new RecipeHandlerList(io);
        rhl.addHandler(handler);
        return rhl;
    }

    public void addHandler(IRecipeHandler<?> handler) {
        addHandlers(List.of(handler));
    }

    public void addHandlers(IRecipeHandler<?>... handlers) {
        addHandlers(Arrays.asList(handlers));
    }

    public void addHandlers(List<IRecipeHandler<?>> handlers) {
        for (var handler : handlers) {
            getHandlerMap().computeIfAbsent(handler.getCapability(), c -> new ArrayList<>()).add(handler);
            allHandlers.add(handler);
            if (handler instanceof IRecipeHandlerTrait<?> rht) allHandlerTraits.add(rht);
        }
        if (handlerIO == IO.OUT) sort();
    }

    private void sort() {
        for (var list : getHandlerMap().values()) {
            list.sort(IRecipeHandler.ENTRY_COMPARATOR);
        }
    }

    public void setDistinct(boolean distinct) {
        if (isDistinct != distinct) {
            isDistinct = distinct;
            for (var rht : allHandlerTraits) {
                if (rht instanceof NotifiableRecipeHandlerTrait<?> nrht) {
                    nrht.setDistinct(distinct);
                }
            }
        }
    }

    public boolean hasCapability(RecipeCapability<?> cap) {
        return getHandlerMap().containsKey(cap);
    }

    public List<IRecipeHandler<?>> getCapability(RecipeCapability<?> cap) {
        return getHandlerMap().getOrDefault(cap, Collections.emptyList());
    }

    public boolean isValid(IO extIO) {
        if (this == NO_DATA) return false;
        return (extIO == IO.BOTH || handlerIO == IO.BOTH || extIO == handlerIO);
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

    public Map<RecipeCapability<?>, List> handleRecipe(IO io, GTRecipe recipe, Map<RecipeCapability<?>, List> contents,
                                                       boolean simulate) {
        if (getHandlerMap().isEmpty()) return contents;
        var copy = new Reference2ObjectOpenHashMap<>(contents);
        var it = copy.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            var handlerList = getHandlerMap().get(entry.getKey());
            if (handlerList == null) continue;
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

    private record Subscription(List<ISubscription> subs) implements ISubscription {

        @Override
        public void unsubscribe() {
            subs.forEach(ISubscription::unsubscribe);
        }
    }

    public ISubscription subscribe(Runnable listener) {
        List<ISubscription> subs = new ArrayList<>(allHandlerTraits.size());
        allHandlerTraits.forEach(rht -> subs.add(rht.addChangedListener(listener)));
        return new Subscription(subs);
    }

    public ISubscription subscribe(Runnable listener, RecipeCapability<?> cap) {
        var capList = getCapability(cap);
        List<ISubscription> subs = new ArrayList<>(capList.size());
        for (var handler : capList) {
            if (handler instanceof IRecipeHandlerTrait<?> trait) {
                subs.add(trait.addChangedListener(listener));
            }
        }
        return new Subscription(subs);
    }
}
