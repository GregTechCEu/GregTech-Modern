package com.gregtechceu.gtceu.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Manages the recipe addition lifecycle as recipes are added to an {@link StagingRecipeDB}
 * and later baked into a {@link RecipeDB}
 */
@ApiStatus.Internal
@RequiredArgsConstructor
public final class RecipeAdditionHandler {

    private final @NotNull StagingRecipeDB stagingDB = new StagingRecipeDB();
    private final @NotNull RecipeDB db;

    private boolean isStaging;

    /**
     * Begin the staging process
     */
    @ApiStatus.Internal
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
    @ApiStatus.Internal
    public void addStaging(@NotNull GTRecipe recipe) {
        if (!isStaging) {
            throw new IllegalStateException("cannot add a staging recipe while not in staging state");
        }
        stagingDB.add(recipe);
    }

    /**
     * Complete the staging DB and bake it into an optimized storage
     */
    @ApiStatus.Internal
    public void completeStaging() {
        if (!isStaging) {
            throw new IllegalStateException("cannot complete staging while not in staging state");
        }
        db.clear();
        stagingDB.populateDB(db);
        stagingDB.clear();
        this.isStaging = false;
    }
}
