package com.gregtechceu.gtceu.api.recipe.lookup;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
public final class StagingRecipeDB {

    private final @NotNull ObjectOpenHashSet<GTRecipeDefinition> recipes = new ObjectOpenHashSet<>();

    public boolean add(@NotNull GTRecipeDefinition recipe) {
        return recipes.add(recipe);
    }

    public void clear() {
        recipes.clear();
        recipes.trim();
    }

    public void populateDB(@NotNull RecipeDB db) {
        // var frequencies = inputFrequencies();
        for (GTRecipeDefinition recipe : recipes) {
            List<List<AbstractMapIngredient>> flattedContent = flattenedContent(recipe);
            // flattedContent.sort(Comparator.comparingInt(frequencies::getInt));
            List<List<AbstractMapIngredient>> inputs = new ArrayList<>(flattedContent.size());
            for (var ingredients : flattedContent) {
                MapIngredientPool.applyPooling(ingredients);
                inputs.add(ingredients);
            }
            boolean result = db.add(recipe, inputs);
            if (!result) {
                GTCEu.LOGGER.warn("failed to add recipe from staging into lookup DB: {}", recipe.getId());
            }
        }
    }

    private @NotNull Object2IntMap<List<AbstractMapIngredient>> inputFrequencies() {
        var map = new Object2IntOpenHashMap<List<AbstractMapIngredient>>();
        for (GTRecipeDefinition recipe : recipes) {
            recipe.getInputMapIngredients().forEach(list -> map.mergeInt(list, 1, Integer::sum));
            recipe.getTickInputMapIngredients().forEach(list -> map.mergeInt(list, 1, Integer::sum));
        }
        return map;
    }

    private static @NotNull List<List<AbstractMapIngredient>> flattenedContent(@NotNull GTRecipeDefinition recipe) {
        List<List<AbstractMapIngredient>> list = new ArrayList<>();
        list.addAll(recipe.getInputMapIngredients());
        list.addAll(recipe.getTickInputMapIngredients());
        return list;
    }
}
