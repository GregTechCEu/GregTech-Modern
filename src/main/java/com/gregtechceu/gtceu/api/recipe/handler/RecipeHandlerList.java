package com.gregtechceu.gtceu.api.recipe.handler;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RecipeHandlerList {

    public static final int UNDYED = -1;

    public static final RecipeHandlerList NO_DATA = new RecipeHandlerList(-1, false, List.of());

    public static final Comparator<RecipeHandlerList> COMPARATOR = (h1, h2) -> {
        int cmp = Boolean.compare(h1.isDistinct, h2.isDistinct);
        if (cmp != 0) return cmp;
        boolean b1 = h1.color != UNDYED;
        boolean b2 = h2.color != UNDYED;
        return Boolean.compare(b1, b2);
    };

    @Getter
    private final List<IRecipeHandler<?>> allHandlers = new ArrayList<>();

    @Getter
    private final int color;

    @Getter
    private final boolean isDistinct;

    protected RecipeHandlerList(int color, boolean isDistinct, Iterable<IRecipeHandler<?>> handlers) {
        this.color = color;
        this.isDistinct = isDistinct;
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
        return color != UNDYED;
    }

    public List<IRecipeHandler<?>> getCapability(RecipeCapability<?> cap) {
        return getAllHandlers().stream().filter(h -> h.getCapability() == cap).toList();
    }

    public static RecipeHandlerList of(Iterable<IRecipeHandler<?>> handlers) {
        return new RecipeHandlerList(UNDYED, false, handlers);
    }

    public static RecipeHandlerList of(int color, Iterable<IRecipeHandler<?>> handlers) {
        return new RecipeHandlerList(color, false, handlers);
    }

    public static RecipeHandlerList of(int color, boolean isDistinct, Iterable<IRecipeHandler<?>> handlers) {
        return new RecipeHandlerList(color, isDistinct, handlers);
    }

}
