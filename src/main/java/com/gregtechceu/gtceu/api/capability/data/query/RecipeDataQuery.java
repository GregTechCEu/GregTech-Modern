package com.gregtechceu.gtceu.api.capability.data.query;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class RecipeDataQuery extends DataQueryObject {

    @Getter
    private final GTRecipe recipe;

    private boolean found = false;

    public RecipeDataQuery(GTRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public @NotNull DataQueryFormat getFormat() {
        return DataQueryFormat.RECIPE;
    }

    public void setFound() {
        this.found = true;
        this.setShouldTriggerWalker(true);
    }

    public boolean isFound() {
        return found;
    }
}
