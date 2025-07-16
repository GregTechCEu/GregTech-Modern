package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

public interface IRecipeHandler<K> extends IFilteredHandler<K> {

    /**
     * Comparator for entries that can be used in insertion logic
     */
    Comparator<IRecipeHandler<?>> ENTRY_COMPARATOR = (o1, o2) -> {
        // #1: Filter priority, like locked slots, first
        int prio = IFilteredHandler.PRIORITY_COMPARATOR.compare(o1, o2);
        if (prio != 0) return prio;
        // #2: Then use non-empty storage
        boolean empty1 = o1.getTotalContentAmount() <= 0;
        boolean empty2 = o2.getTotalContentAmount() <= 0;
        return Boolean.compare(empty1, empty2);
    };

    /**
     * matching or handling the given recipe.
     *
     * @param io       the IO type of this recipe. always be one of the {@link IO#IN} or {@link IO#OUT}
     * @param recipe   recipe.
     * @param left     left contents for to be handled.
     * @param simulate simulate.
     * @return left contents for continue handling by other proxies.
     *         <br>
     *         null - nothing left. handling successful/finish. you should always return null as a handling-done mark.
     */
    List<K> handleRecipeInner(IO io, GTRecipe recipe, List<K> left, boolean simulate);

    /**
     * container size, if it has one. otherwise -1.
     */
    default int getSize() {
        return -1;
    }

    @NotNull
    List<Object> getContents();

    double getTotalContentAmount();

    /**
     * Whether the content of same capability can only be handled distinct.
     */
    default boolean isDistinct() {
        return false;
    }

    /**
     * Returns {@code true} if this {@code IRecipeHandler} has content to be searched.
     * The main use of this is differentiating circuit inventories from item inventories
     * 
     * @return {@code true} if this {@code IRecipeHandler} has content to be searched
     */
    default boolean shouldSearchContent() {
        return true;
    }

    RecipeCapability<K> getCapability();

    @SuppressWarnings("unchecked")
    default K copyContent(Object content) {
        return getCapability().copyInner((K) content);
    }

    default List<K> handleRecipe(IO io, GTRecipe recipe, List<?> left, boolean simulate) {
        List<K> contents = new ObjectArrayList<>(left.size());
        for (Object leftObj : left) {
            contents.add(copyContent(leftObj));
        }
        return handleRecipeInner(io, recipe, contents, simulate);
    }
}
