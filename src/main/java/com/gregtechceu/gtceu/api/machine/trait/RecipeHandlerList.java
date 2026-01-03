package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
    private final List<NotifiableRecipeHandlerTrait<?>> allHandlerTraits = new ArrayList<>();

    @Getter
    private final IO handlerIO;
    @Getter
    private int color = -1;

    @Setter
    @Getter
    @NotNull
    private RecipeHandlerGroup group = RecipeHandlerGroupColor.UNDYED;

    protected RecipeHandlerList(IO handlerIO) {
        this.handlerIO = handlerIO;
    }

    public static RecipeHandlerList of(IO io, int color, IRecipeHandler<?>... handlers) {
        RecipeHandlerList rhl = new RecipeHandlerList(io);
        rhl.addHandlers(handlers);
        rhl.setColor(color);
        return rhl;
    }

    public static RecipeHandlerList of(IO io, IRecipeHandler<?>... handlers) {
        RecipeHandlerList rhl = new RecipeHandlerList(io);
        rhl.addHandlers(handlers);
        return rhl;
    }

    public static RecipeHandlerList of(IO io, Iterable<IRecipeHandler<?>> handlers) {
        RecipeHandlerList rhl = new RecipeHandlerList(io);
        rhl.addHandlers(handlers);
        return rhl;
    }

    public static RecipeHandlerList of(IO io, int color, Iterable<IRecipeHandler<?>> handlers) {
        RecipeHandlerList rhl = new RecipeHandlerList(io);
        rhl.addHandlers(handlers);
        rhl.setColor(color);
        return rhl;
    }

    public void addHandler(IRecipeHandler<?> handler) {
        addHandlers(List.of(handler));
    }

    public void addHandlers(IRecipeHandler<?>... handlers) {
        addHandlers(Arrays.asList(handlers));
    }

    public void addHandlers(Iterable<IRecipeHandler<?>> handlers) {
        for (var handler : handlers) {
            getHandlerMap().computeIfAbsent(handler.getCapability(), c -> new ArrayList<>()).add(handler);
            allHandlers.add(handler);
            if (handler instanceof NotifiableRecipeHandlerTrait<?> rht) allHandlerTraits.add(rht);
        }
        if (handlerIO.support(IO.OUT)) sort();
    }

    private void sort() {
        for (var list : getHandlerMap().values()) {
            list.sort(IRecipeHandler.ENTRY_COMPARATOR);
        }
    }

    public final void setDistinctAndNotify(boolean distinct) {
        setDistinct(distinct, true);
    }

    public final void setDistinct(boolean distinct) {
        setDistinct(distinct, false);
    }

    protected void setDistinct(boolean distinct, boolean notify) {
        boolean currentDistinct = isDistinct();
        if (currentDistinct != distinct) {
            this.group = currentDistinct ? new RecipeHandlerGroupColor(color) :
                    RecipeHandlerGroupDistinctness.BUS_DISTINCT;
            for (var rht : allHandlerTraits) {
                rht.setDistinct(distinct);
                if (notify) rht.notifyListeners();
            }
        }
    }

    public boolean isDistinct() {
        return this.group == RecipeHandlerGroupDistinctness.BUS_DISTINCT;
    }

    public void setColor(int color) {
        setColor(color, false);
    }

    public void setColor(int color, boolean notify) {
        this.color = color;
        if (this.group != RecipeHandlerGroupDistinctness.BUS_DISTINCT) {
            this.group = new RecipeHandlerGroupColor(color);
        }
        if (notify) {
            for (var rht : allHandlerTraits) {
                rht.notifyListeners();
            }
        }
    }

    public boolean hasCapability(RecipeCapability<?> cap) {
        return getHandlerMap().containsKey(cap);
    }

    public @NotNull List<IRecipeHandler<?>> getCapability(RecipeCapability<?> cap) {
        return getHandlerMap().getOrDefault(cap, Collections.emptyList());
    }

    public @NotNull Set<RecipeCapability<?>> getCapabilities() {
        return getHandlerMap().keySet();
    }

    /**
     * @return whether any of the capabilities in this RHL should bypass distinct checks
     */
    public boolean doesCapabilityBypassDistinct() {
        for (var capability : getCapabilities()) {
            if (capability.shouldBypassDistinct()) return true;
        }
        return false;
    }

    public boolean isValid(IO extIO) {
        if (this == NO_DATA || handlerIO == IO.NONE) return false;
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

    @Contract(pure = true)
    public Map<RecipeCapability<?>, List<Object>> handleRecipe(IO io, GTRecipe recipe,
                                                               Map<RecipeCapability<?>, List<Object>> contents,
                                                               boolean simulate) {
        if (getHandlerMap().isEmpty()) return contents;
        var copy = new Reference2ObjectOpenHashMap<>(contents);
        for (var it = copy.reference2ObjectEntrySet().fastIterator(); it.hasNext();) {
            var entry = it.next();
            var handlerList = getCapability(entry.getKey());
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

    public List<IRecipeHandler<?>> getHandlersFlat() {
        List<IRecipeHandler<?>> handlerList = new ArrayList<>();
        for (var handlerEntry : getHandlerMap().entrySet()) {
            handlerList.addAll(handlerEntry.getValue());
        }
        return handlerList;
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
