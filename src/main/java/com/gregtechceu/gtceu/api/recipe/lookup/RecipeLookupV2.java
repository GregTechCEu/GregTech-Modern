package com.gregtechceu.gtceu.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class RecipeLookupV2 {

    private final StagingRecipeDB stagingDB;
    private final RecipeDB recipeDB;
    private boolean isStaging;

    public RecipeLookupV2(@NotNull GTRecipeType recipeType) {
        this.stagingDB = new StagingRecipeDB(recipeType);
        this.recipeDB = new RecipeDB(recipeType);
    }

    /**
     * Begin the staging process
     */
    public void beginStaging() {
        if (isStaging) {
            throw new IllegalStateException("cannot begin staging while already in staging state");
        }
        this.isStaging = true;
    }

    /**
     * Add a recipe to the staging DB
     *
     * @param recipe the recipe
     */
    public void addStaging(@NotNull GTRecipe recipe) {
        if (!isStaging) {
            throw new IllegalStateException("cannot add a staging recipe while not in staging state");
        }
        stagingDB.add(recipe);
    }

    /**
     * Complete the staging DB and bake it into an optimized storage
     */
    public void completeStaging() {
        if (!isStaging) {
            throw new IllegalStateException("cannot complete staging while not in staging state");
        }
        recipeDB.clear();
        stagingDB.populateDB(recipeDB);
        stagingDB.clear();
        MapIngredientPool.clear();
        this.isStaging = false;
    }
}
