package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public interface IDataAccessHatch {

    /**
     * If passed a {@code seen} context, you must use {@link #isRecipeAvailable(GTRecipe, Collection)} to prevent
     * infinite recursion
     *
     * @param recipe the recipe to check
     * @return if the recipe is available for use
     */
    default boolean isRecipeAvailable(@NotNull GTRecipe recipe) {
        Collection<IDataAccessHatch> list = new ArrayList<>();
        list.add(this);
        return isRecipeAvailable(recipe, list);
    }

    /**
     * @param recipe the recipe to check
     * @param seen   the hatches already checked
     * @return if the recipe is available for use
     */
    boolean isRecipeAvailable(@NotNull GTRecipe recipe, @NotNull Collection<IDataAccessHatch> seen);

    // REMEMBER TO CALL THIS FOR YOUR CUSTOM DATA HATCH TO WORK.
    default Component modifyRecipe(GTRecipe recipe) {
        // creative hatches do not need to check, they always have the recipe
        if (this.isCreative()) return null;

        // hatches need to have the recipe available
        if (this.isRecipeAvailable(recipe)) return null;
        return Component.translatable("gtceu.recipe_modifier.missing_research_data");
    }

    /**
     * @return true if this Data Access Hatch is creative or not
     */
    boolean isCreative();
}
