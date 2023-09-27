package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote IRecipeHandler
 */
public interface IRecipeHandler<K> {

    /**
     * matching or handling the given recipe.
     *
     * @param io       the IO type of this recipe. always be one of the {@link IO#IN} or {@link IO#OUT}
     * @param recipe   recipe.
     * @param left     left contents for to be handled.
     * @param slotName specific slot name.
     * @param simulate simulate.
     * @return left contents for continue handling by other proxies.
     * <br>
     * null - nothing left. handling successful/finish. you should always return null as a handling-done mark.
     */
    List<K> handleRecipeInner(IO io, GTRecipe recipe, List<K> left, @Nullable String slotName, boolean simulate);

    /**
     * The timestamp indicates the time in the world when the last change occurred
     */
    long getTimeStamp();

    /**
     * Update to the latest work time.
     */
    void setTimeStamp(long timeStamp);

    default void updateTimeStamp(@Nullable Level level) {
        if (level != null) {
            setTimeStamp(level.getGameTime());
        }
    }

    /**
     * Slot name, it makes sense if recipe contents specify a slot name.
     */
    @Nullable
    default Set<String> getSlotNames() {
        return null;
    }

    /**
     * Whether the content of same capability  can only be handled distinct.
     */
    default boolean isDistinct() {
        return false;
    }

    RecipeCapability<K> getCapability();

    @SuppressWarnings("unchecked")
    default K copyContent(Object content) {
        return getCapability().copyInner((K)content);
    }

    default List<K> handleRecipe(IO io, GTRecipe recipe, List<?> left, @Nullable String slotName, boolean simulate) {
        return handleRecipeInner(io, recipe, left.stream().map(this::copyContent).collect(Collectors.toList()), slotName, simulate);
    }

    default void preWorking(IRecipeCapabilityHolder holder, IO io, GTRecipe recipe) {
    }

    default void postWorking(IRecipeCapabilityHolder holder, IO io, GTRecipe recipe) {
    }


}
