package com.gregtechceu.gtceu.api.recipe.lookup;

import com.google.common.base.Preconditions;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.MapIngredientTypeManager;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.*;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.*;

@ApiStatus.Internal
@RequiredArgsConstructor
final class StagingRecipeDB {

    private final ObjectOpenHashSet<GTRecipe> recipes = new ObjectOpenHashSet<>();
    private final @NotNull GTRecipeType recipeType;

    public boolean add(@NotNull GTRecipe recipe) {
        return recipes.add(recipe);
    }

    /**
     * Clear the DB
     */
    public void clear() {
        recipes.clear();
        recipes.trim();
    }

    /**
     * Populate a DB with the contents of the staging DB
     *
     * @param db the db to populate
     */
    public void populateDB(@NotNull RecipeDB db) {
        Preconditions.checkArgument(db.getRecipeType() == recipeType, "cannot populate RecipeDB with incompatible RecipeTypes");
        var frequencies = inputFrequencies();
        for (GTRecipe recipe : recipes) {
            List<Pair<RecipeCapability<?>, Object>> flattedContent = flattenedContent(recipe);
            flattedContent.sort(Comparator.comparingInt(entry -> frequencies.getInt(entry.right())));
            List<List<AbstractMapIngredient>> inputs = new ArrayList<>(flattedContent.size());
            for (var entry : flattedContent) {
                var ingredients = MapIngredientTypeManager.getFrom(entry.right(), entry.left());
                MapIngredientPool.applyPooling(ingredients);
                inputs.add(ingredients);
            }
            boolean result = db.add(recipe, inputs);
            if (!result) {
                GTCEu.LOGGER.warn("failed to add recipe from staging into lookup DB: {}", recipe.getId());
            }
        }
    }

    /**
     * @return a map of the amount of times every input is used
     */
    private @NotNull Object2IntMap<Object> inputFrequencies() {
        var map = new Object2IntOpenHashMap<>();
        for (GTRecipe recipe : recipes) {
            recipe.inputs.forEach((cap, list) -> {
                for (var input : compressedContent(list, cap)) {
                    map.mergeInt(input, 1, Integer::sum);
                }
            });
            recipe.tickInputs.forEach((cap, list) -> {
                for (var input : compressedContent(list, cap)) {
                    map.mergeInt(input, 1, Integer::sum);
                }
            });
        }
        return map;
    }

    /**
     * @param list the list of content
     * @param cap the RecipeCapability for the content
     * @return the compressed ingredient form of the content
     */
    private static @NotNull List<Object> compressedContent(@NotNull List<Content> list,
                                                           @NotNull RecipeCapability<?> cap) {
        var contentList = list.stream()
                .map(Content::getContent)
                .toList();
        return cap.compressIngredients(contentList);
    }

    /**
     * Returns the flattened content of a recipe
     *
     * @param recipe the recipe
     * @return the flattened content
     */
    private static @NotNull List<Pair<RecipeCapability<?>, Object>> flattenedContent(@NotNull GTRecipe recipe) {
        var map = new Object2ObjectOpenHashMap<RecipeCapability<?>, List<Content>>();
        recipe.inputs.forEach((cap, list) -> buildInputsByCap(map, cap, list));
        recipe.tickInputs.forEach((cap, list) -> buildInputsByCap(map, cap, list));
        List<Pair<RecipeCapability<?>, Object>> list = new ArrayList<>();
        map.forEach((k, v) -> {
            for (var content : v) {
                list.add(Pair.of(k, content.getContent()));
            }
        });
        return list;
    }

    /**
     * Builds a map of inputs by RecipeCapability
     *
     * @param map  the map to populate
     * @param cap  the recipe capability for the list
     * @param list the list of inputs
     */
    private static void buildInputsByCap(@NotNull Map<RecipeCapability<?>, List<Content>> map,
                                         @NotNull RecipeCapability<?> cap, @NotNull List<Content> list) {
        map.compute(cap, (k, v) -> {
            if (v == null) {
                return new ArrayList<>(list);
            }
            v.addAll(list);
            return v;
        });
    }
}
