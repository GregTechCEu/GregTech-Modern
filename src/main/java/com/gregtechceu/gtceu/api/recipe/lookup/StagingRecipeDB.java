package com.gregtechceu.gtceu.api.recipe.lookup;

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
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.*;

@RequiredArgsConstructor
public final class StagingRecipeDB {

    private static final Map<AbstractMapIngredient, WeakReference<AbstractMapIngredient>> ingredientCache = new WeakHashMap<>();

    private final Set<GTRecipe> recipes = new ObjectOpenHashSet<>();
    private final @NotNull GTRecipeType recipeType;

    public boolean add(@NotNull GTRecipe recipe) {
        return recipes.add(recipe);
    }

    public void removeAll() {
        recipes.clear();
    }

    private @NotNull RecipeDB buildLookup() {
        RecipeDB db = new RecipeDB(recipeType);
        var frequencies = inputFrequencies();
        for (GTRecipe recipe : recipes) {
            var map = new Object2ObjectOpenHashMap<RecipeCapability<?>, List<Content>>();
            // noinspection CodeBlock2Expr
            recipe.inputs.forEach((cap, list) -> {
                map.compute(cap, (k, v) -> {
                    if (v == null) {
                        return new ArrayList<>(list);
                    }
                    v.addAll(list);
                    return v;
                });
            });
            // noinspection CodeBlock2Expr
            recipe.tickInputs.forEach((cap, list) -> {
                map.compute(cap, (k, v) -> {
                    if (v == null) {
                        return new ArrayList<>(list);
                    }
                    v.addAll(list);
                    return v;
                });
            });
            List<Pair<RecipeCapability<?>, Object>> flattedContent = new ArrayList<>();
            map.forEach((k, v) -> {
                for (var content : v) {
                    flattedContent.add(Pair.of(k, content.getContent()));
                }
            });
            flattedContent.sort(Comparator.comparingInt(entry -> frequencies.getInt(entry.right())));
            List<List<AbstractMapIngredient>> inputs = new ArrayList<>(flattedContent.size());
            for (var entry : flattedContent) {
                var ingredients = MapIngredientTypeManager.getFrom(entry.right(), entry.left());
                applyCachedIngredients(ingredients);
                inputs.add(ingredients);
            }
            boolean result = db.add(recipe, inputs);
            if (!result) {
                GTCEu.LOGGER.warn("failed to add recipe into permanent DB: {}", recipe.getId());
            }
        }
        return db;
    }

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

    private static @NotNull List<Object> compressedContent(@NotNull List<Content> list,
                                                           @NotNull RecipeCapability<?> cap) {
        var contentList = list.stream()
                .map(Content::getContent)
                .toList();
        return cap.compressIngredients(contentList);
    }

    /**
     * Updates a list of ingredients with cached versions.
     * If there is no cached instance, the value in the list becomes cached.
     *
     * @param ingredients the ingredient instances to deduplicate
     */
    private static void applyCachedIngredients(@NotNull List<AbstractMapIngredient> ingredients) {
        for (int i = 0; i < ingredients.size(); i++) {
            AbstractMapIngredient mappedIngredient = ingredients.get(i);
            WeakReference<AbstractMapIngredient> cached = ingredientCache.get(mappedIngredient);
            if (cached != null) {
                var ingredient = cached.get();
                if (ingredient != null) {
                    ingredients.set(i, ingredient);
                    continue;
                }
            }
            ingredientCache.put(mappedIngredient, new WeakReference<>(mappedIngredient));
        }
    }
}
