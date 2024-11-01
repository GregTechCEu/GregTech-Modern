package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote IRecipeHandler
 */
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
     * @param slotName specific slot name.
     * @param simulate simulate.
     * @return left contents for continue handling by other proxies.
     *         <br>
     *         null - nothing left. handling successful/finish. you should always return null as a handling-done mark.
     */
    List<K> handleRecipeInner(IO io, GTRecipe recipe, List<K> left, @Nullable String slotName, boolean simulate);

    /**
     * Slot name, it makes sense if recipe contents specify a slot name.
     */
    @Nullable
    default Set<String> getSlotNames() {
        return null;
    }

    /**
     * container size, if it has one. otherwise -1.
     */
    default int getSize() {
        return -1;
    }

    List<Object> getContents();

    double getTotalContentAmount();

    /**
     * Whether the content of same capability can only be handled distinct.
     */
    default boolean isDistinct() {
        return false;
    }

    default boolean isProxy() {
        return false;
    }

    RecipeCapability<K> getCapability();

    @SuppressWarnings("unchecked")
    default K copyContent(Object content) {
        return getCapability().copyInner((K) content);
    }

    default List<K> handleRecipe(IO io, GTRecipe recipe, List<?> left, @Nullable String slotName, boolean simulate) {
        List<K> contents = new ObjectArrayList<>(left.size());
        for (Object leftObj : left) {
            contents.add(copyContent(leftObj));
        }
        return handleRecipeInner(io, recipe, contents, slotName, simulate);
    }

    default void preWorking(IRecipeCapabilityHolder holder, IO io, GTRecipe recipe) {}

    default void postWorking(IRecipeCapabilityHolder holder, IO io, GTRecipe recipe) {}
}
