package com.gregtechceu.gtceu.api.recipe.handler;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class RecipeHandlerList {

    public static final int UNDYED = -1;

    public static final RecipeHandlerList NO_DATA = new RecipeHandlerList(() -> UNDYED, () -> false, List.of());

    public static final Comparator<RecipeHandlerList> COMPARATOR = (h1, h2) -> {
        int cmp = Boolean.compare(h1.isDistinct(), h2.isDistinct());
        if (cmp != 0) return cmp;
        boolean b1 = h1.isDyed();
        boolean b2 = h2.isDyed();
        return Boolean.compare(b1, b2);
    };

    @Getter
    private final List<IRecipeHandler<?>> allHandlers = new ArrayList<>();

    @Getter
    private final Supplier<Integer> colorSupplier;

    @Getter
    private final Supplier<Boolean> distinctSupplier;

    protected RecipeHandlerList(Supplier<Integer> colorSupplier, Supplier<Boolean> distinctSupplier, Iterable<IRecipeHandler<?>> handlers) {
        this.colorSupplier = colorSupplier;
        this.distinctSupplier = distinctSupplier;
        addHandlers(handlers);
    }

    private void addHandlers(Iterable<IRecipeHandler<?>> handlers) {
        for (var handler : handlers) {
            allHandlers.add(handler);
        }
    }

    public void addHandlers(IRecipeHandler<?>... handlers) {
        addHandlers(List.of(handlers));
    }

    public boolean isDyed() {
        return getColor() != UNDYED;
    }

    public int getColor() {
        return colorSupplier.get();
    }

    public boolean isDistinct() {
        return distinctSupplier.get();
    }

    public List<IRecipeHandler<?>> getCapability(RecipeCapability<?> cap) {
        var list = new ArrayList<IRecipeHandler<?>>();
        for(var handler: getAllHandlers()) {
            if(handler.getCapability() == cap) list.add(handler);
        }
        return list;
    }

    public static RecipeHandlerList of(Iterable<IRecipeHandler<?>> handlers) {
        return new RecipeHandlerList(() -> UNDYED, () -> false, handlers);
    }

    public static RecipeHandlerList of(Supplier<Integer> color, Iterable<IRecipeHandler<?>> handlers) {
        return new RecipeHandlerList(color, () -> false, handlers);
    }

    public static RecipeHandlerList of(Supplier<Integer> color, Supplier<Boolean> isDistinct, Iterable<IRecipeHandler<?>> handlers) {
        return new RecipeHandlerList(color, isDistinct, handlers);
    }

    public ISubscription subscribe(Runnable listener) {
        List<ISubscription> subs = new ArrayList<>(allHandlers.size());
        for(var handler : allHandlers) {
            if(handler instanceof NotifiableRecipeHandlerTrait<?> trait) {
                subs.add(trait.addChangedListener(listener));
            }
        }
        return new Subscription(subs);
    }

    public ISubscription subscribe(Runnable listener, RecipeCapability<?> cap) {
        var capList = getCapability(cap);
        List<ISubscription> subs = new ArrayList<>(allHandlers.size());
        for (var handler : capList) {
            if (handler instanceof NotifiableRecipeHandlerTrait<?> trait) {
                subs.add(trait.addChangedListener(listener));
            }
        }
        return new Subscription(subs);
    }

    private record Subscription(List<ISubscription> subs) implements ISubscription {

        @Override
        public void unsubscribe() {
            subs.forEach(ISubscription::unsubscribe);
        }
    }
}
