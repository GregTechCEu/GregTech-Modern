package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import org.jetbrains.annotations.NotNull;

public interface IDataAccessMachine {

    /**
     * @param recipe the recipe to check
     * @return if the recipe is available for use
     */
     boolean isRecipeAvailable(@NotNull GTRecipe recipe);

     default void notifyListeners() {}

}
