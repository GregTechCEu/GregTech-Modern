package com.gregtechceu.gtceu.api.machine.trait.feature;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import org.jetbrains.annotations.Nullable;

/**
 * A machine trait that modifies the behaviour of the recipe logic instance attached to a machine
 */
@SuppressWarnings("unused")
public interface IRecipeLogicModifierTrait {

    /**
     * Called when the recipe logic status changes
     * @param oldStatus Old recipe logic status
     * @param newStatus New recipe logic status
     */
    default void recipeLogicStatusChanged(RecipeLogic.Status oldStatus, RecipeLogic.Status newStatus) {}

    /**
     * Override to modify recipe on the fly e.g. applying overclock, change chance, etc
     *
     * @param recipe The current recipe
     * @return modified recipe.
     *         null -- this recipe is unavailable
     */
    @Nullable
    default GTRecipe modifyRecipe(GTRecipe recipe) { return recipe; }

    /**
     * Called when a recipe is about to be run, just before inputs are consumed.
     *
     * @return true to cancel the recipe, false to continue
     *
     * @see RecipeLogic#setupRecipe(GTRecipe)
     */
    default boolean beforeWorking(@Nullable GTRecipe recipe) {
        return true;
    }

    /**
     * Called every tick while the recipe is working.
     *
     * @return true to interrupt and suspend the recipe, false to continue working
     *
     * @see RecipeLogic#handleRecipeWorking()
     */
    default boolean onWorking() {
        return true;
    }

    /**
     * Called when the recipe finishes, before outputs are produced.
     *
     * @see RecipeLogic#onRecipeFinish()
     */
    default void afterWorking() {}
}
