package com.gregtechceu.gtceu.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import org.jetbrains.annotations.NotNull;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class RecipeIterator implements Iterator<GTRecipe> {

    int index;
    List<List<AbstractMapIngredient>> ingredients;
    @NotNull
    GTRecipeType recipeMap;
    @NotNull
    Predicate<GTRecipe> canHandle;

    RecipeIterator(@NotNull GTRecipeType recipeMap, List<List<AbstractMapIngredient>> ingredients,
                   @NotNull Predicate<GTRecipe> canHandle) {
        this.ingredients = ingredients;
        this.recipeMap = recipeMap;
        this.canHandle = canHandle;
    }

    // does not guarantee a next recipe, just the possibility of one
    @Override
    public boolean hasNext() {
        return ingredients != null && this.index < this.ingredients.size();
    }

    @Override
    public GTRecipe next() {
        // couldn't build any inputs to use for search, so no recipe could be found
        if (ingredients == null) return null;
        // Try each ingredient as a starting point, save current index
        GTRecipe r = null;
        while (index < ingredients.size()) {
            BitSet skipSet = new BitSet();
            skipSet.set(index);
            r = recipeMap.getLookup().recurseIngredientTreeFindRecipe(ingredients,
                    recipeMap.getLookup().getLookup(), canHandle,
                    index, 0, skipSet);
            ++index;
            if (r != null) break;
        }
        return r;
    }

    public void reset() {
        index = 0;
    }
}
