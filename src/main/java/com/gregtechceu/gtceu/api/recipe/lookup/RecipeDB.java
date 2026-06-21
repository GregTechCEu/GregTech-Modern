package com.gregtechceu.gtceu.api.recipe.lookup;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.MapIngredientTypeManager;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.item.armor.PowerlessJetpack;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraftforge.registries.ForgeRegistries;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.Predicate;

/**
 * Data structure storing recipes by their input ingredients
 */
public final class RecipeDB {

    private final @NotNull Branch rootBranch = new Branch();

    /**
     * Clear the DB
     */
    @ApiStatus.Internal
    public void clear() {
        rootBranch.clear();
    }

    /**
     * Find a GT Recipe
     *
     * @param holder the holder to search
     * @return the recipe
     */
    public @Nullable GTRecipe find(@NotNull IRecipeCapabilityHolder holder) {
        return find(holder, r -> RecipeHelper.matchRecipe(holder, r).isSuccess());
    }

    /**
     * Find a GT Recipe
     *
     * @param holder    the holder to search
     * @param predicate the predicate to determine recipe validity
     * @return the recipe
     */
    public @Nullable GTRecipe find(@NotNull IRecipeCapabilityHolder holder, @NotNull Predicate<GTRecipe> predicate) {
        List<List<AbstractMapIngredient>> list = fromHolder(holder);
        if (list == null) {
            return null;
        }
        return find(list, predicate);
    }

    /**
     * Find a GT Recipe
     *
     * @param list      the ingredients to search
     * @param predicate the predicate to determine recipe validity
     * @return the recipe
     */
    @ApiStatus.Internal
    @VisibleForTesting
    public @Nullable GTRecipe find(@NotNull List<List<AbstractMapIngredient>> list,
                                   @NotNull Predicate<GTRecipe> predicate) {
        var iter = new RecipeIterator(this, list, predicate);
        return iter.hasNext() ? iter.next() : null;
    }

    /**
     * Find a GT Recipe
     *
     * @param inputs    the input capabilities and their associated contents to search with
     * @param predicate the predicate to determine recipe validity
     * @return the recipe
     */
    public @Nullable GTRecipe find(@NotNull Map<RecipeCapability<?>, List<Object>> inputs,
                                   @NotNull Predicate<GTRecipe> predicate) {
        List<List<AbstractMapIngredient>> list = new ArrayList<>();
        inputs.forEach((cap, content) -> {
            if (!cap.isRecipeSearchFilter()) {
                return;
            }
            var compressed = cap.compressIngredients(content);
            for (var ingredient : compressed) {
                list.add(MapIngredientTypeManager.getFrom(ingredient, cap));
            }
        });
        return find(list, predicate);
    }

    /**
     * Create an iterator for a search space
     *
     * @param holder    the holder to search
     * @param predicate the predicate to determine recipe validity
     * @return an iterator
     */
    public @Nullable RecipeDB.RecipeIterator iterator(@NotNull IRecipeCapabilityHolder holder,
                                                      @NotNull Predicate<GTRecipe> predicate) {
        List<List<AbstractMapIngredient>> list = fromHolder(holder);
        if (list == null) {
            return null;
        }
        return new RecipeIterator(this, list, predicate);
    }

    /**
     * Converts a Recipe Capability holder's handlers into a list of {@link AbstractMapIngredient}
     *
     * @param holder the capability holder to query handlers from
     * @return a list of all the AbstractMapIngredients in the handlers
     */
    private @Nullable List<List<AbstractMapIngredient>> fromHolder(@NotNull IRecipeCapabilityHolder holder) {
        var handlerMap = holder.getCapabilitiesFlat().getOrDefault(IO.IN, Collections.emptyMap());
        if (handlerMap.isEmpty()) {
            return null;
        }

        // the initial capacity is a "feel-good" value because it's faster to just grow the list
        // than to calculate an accurate value.
        List<List<AbstractMapIngredient>> list = new ObjectArrayList<>(handlerMap.size() * 8);
        handlerMap.forEach((cap, handlers) -> {
            if (!cap.isRecipeSearchFilter()) {
                return;
            }
            for (var handler : handlers) {
                var compressed = cap.compressIngredients(handler.getContents());
                for (var ingredient : compressed) {
                    list.add(MapIngredientTypeManager.getFrom(ingredient, cap));
                }
            }
        });
        if (list.isEmpty()) {
            return null;
        }
        return list;
    }

    /**
     * Determine the correct root nodes for an ingredient.
     *
     * @param ingredient the ingredient to check
     * @param branch     the branch containing the nodes
     * @return the nodes to search for the ingredient
     */
    private static @NotNull Map<AbstractMapIngredient, Either<GTRecipe, Branch>> nodesForIngredient(@NotNull AbstractMapIngredient ingredient,
                                                                                                    @NotNull Branch branch) {
        if (ingredient.isSpecialIngredient()) {
            return branch.getSpecialNodes();
        }
        return branch.getNodes();
    }

    /**
     * Add a recipe.
     *
     * @param recipe      the recipe to add
     * @param ingredients the ingredients in optimal order, comprising the recipe
     * @return if successful
     */
    boolean add(@NotNull GTRecipe recipe, @NotNull List<@Unmodifiable List<AbstractMapIngredient>> ingredients) {
        // Add combustion fuels to the Powerless Jetpack
        if (recipe.getType() == GTRecipeTypes.COMBUSTION_GENERATOR_FUELS) {
            Content content = recipe.getInputContents(FluidRecipeCapability.CAP).get(0);
            FluidIngredient fluid = FluidRecipeCapability.CAP.of(content.content());
            PowerlessJetpack.FUELS.putIfAbsent(fluid, recipe.duration);
        }
        if (addRecursive(recipe, ingredients, rootBranch, 0)) {
            recipe.recipeCategory.addRecipe(recipe);
            return true;
        }
        return false;
    }

    /**
     * Recursively adds a recipe.
     *
     * @param recipe      the recipe to add
     * @param ingredients the ingredients to find the recipe with
     * @param branch      the branch to add ingredients to
     * @param index       the index of the ingredient list to check
     * @return if successful
     */
    private boolean addRecursive(@NotNull GTRecipe recipe,
                                 @NotNull List<@Unmodifiable List<AbstractMapIngredient>> ingredients,
                                 @NotNull Branch branch, int index) {
        if (index >= ingredients.size()) {
            return true;
        }
        boolean lastIngredient = index == ingredients.size() - 1;
        var current = ingredients.get(index);
        for (AbstractMapIngredient ingredient : current) {
            var nodes = nodesForIngredient(ingredient, branch);
            var either = nodes.compute(ingredient, (k, v) -> {
                if (lastIngredient) {
                    // last ingredient
                    if (v == null) {
                        // no existing leaf, add the recipe
                        return Either.left(recipe);
                    }
                    if (v.left().isEmpty() || !v.left().get().equals(recipe)) {
                        // empty recipe or different recipe exists already, conflict
                        if (ConfigHolder.INSTANCE.dev.debug || GTCEu.isDev()) {
                            GTCEu.LOGGER.warn(
                                    "Recipe duplicate or conflict found in GTRecipeType {} and was not added. See next lines for details",
                                    ForgeRegistries.RECIPE_TYPES.getKey(recipe.getType()));
                            if (v.left().isPresent()) {
                                GTCEu.LOGGER.warn("Attempted to add GTRecipe: {}, which conflicts with {}",
                                        recipe.getId(), v.left().get().getId());
                            } else {
                                GTCEu.LOGGER.warn("Attempted to add GTRecipe: {}, without exact duplicate/conflict",
                                        recipe.getId());
                            }
                        }
                    }
                    // maintain existing recipe, even on conflicts
                    // if there was no conflict but a recipe was still present, it was added on an earlier recurse,
                    // and this will carry the result further back in the call stack
                    return v;
                }
                // if there is an existing ingredient, use it, otherwise create a new branch for the ingredient
                return Objects.requireNonNullElseGet(v, () -> Either.right(new Branch()));
            });
            if (either.left().isPresent()) {
                if (either.left().get() == recipe) {
                    // recipe was successfully added, continue to add the other paths
                    continue;
                }
                // there was already a recipe here, fail on the conflict
                return false;
            }
            boolean added = either.right()
                    .filter(b -> addRecursive(recipe, ingredients, b, index + 1))
                    .isPresent();
            if (!added) {
                if (lastIngredient) {
                    // remove the recipe
                    nodes.remove(ingredient);
                } else {
                    var child = nodes.get(ingredient);
                    if (child != null && child.right().isPresent()) {
                        var childBranch = child.right().get();
                        if (childBranch.isEmptyBranch()) {
                            // remove the branch if it was the only thing in it
                            nodes.remove(ingredient);
                        }
                    }
                }
                return false;
            }
        }
        return true;
    }

    private static class SearchFrame {

        int index;           // ingredient slot we’re exploring
        int ingredientIndex; // position within ingredients[index]
        Branch branch;       // branch in the recipe DB

        public SearchFrame(int index, Branch branch) {
            this.index = index;
            this.ingredientIndex = 0;
            this.branch = branch;
        }
    }

    public static class RecipeIterator implements Iterator<GTRecipe> {

        private final @NotNull RecipeDB db;
        private final @NotNull List<List<AbstractMapIngredient>> ingredients;
        private final @NotNull Predicate<GTRecipe> predicate;

        private final Deque<SearchFrame> stack = new ArrayDeque<>();

        private @Nullable GTRecipe nextCached = null;
        private boolean hasCached = false;

        @VisibleForTesting
        public RecipeIterator(@NotNull RecipeDB db,
                              @NotNull List<List<AbstractMapIngredient>> ingredients,
                              @NotNull Predicate<GTRecipe> predicate) {
            this.db = db;
            this.ingredients = ingredients;
            this.predicate = predicate;

            for (int i = ingredients.size() - 1; i >= 0; i--) {
                stack.push(new SearchFrame(i, db.rootBranch));
            }
        }

        private @Nullable GTRecipe getNext() {
            while (!stack.isEmpty()) {
                // We stay on one frame until all ingredients have been checked
                SearchFrame frame = stack.peek();

                if (frame.ingredientIndex >= ingredients.get(frame.index).size()) {
                    stack.pop();
                    continue;
                }

                List<AbstractMapIngredient> ingredientList = ingredients.get(frame.index);
                AbstractMapIngredient ingredient = ingredientList.get(frame.ingredientIndex);
                // Increment candidate pos for next iteration
                frame.ingredientIndex++;
                var nodes = nodesForIngredient(ingredient, frame.branch);
                var result = nodes.get(ingredient);
                if (result == null) {
                    continue;
                }

                // Option 1: It's a recipe
                if (result.left().isPresent()) {
                    var recipe = result.left().get();
                    if (predicate.test(recipe)) {
                        return recipe;
                    }
                }

                // Option 2: It's a branch, dive deeper
                result.ifRight(b -> {
                    for (int j = ingredients.size() - 1; j >= 0; j--) {
                        stack.push(new SearchFrame(j, b));
                    }
                });
            }

            return null; // no more recipes
        }

        @Override
        public boolean hasNext() {
            if (!hasCached) {
                nextCached = getNext();
                hasCached = true;
            }
            return nextCached != null;
        }

        @Override
        public GTRecipe next() {
            if (!hasCached) nextCached = getNext();
            hasCached = false;
            if (nextCached == null) throw new NoSuchElementException();
            return nextCached;
        }

        /**
         * Reset the iterator
         */
        public void reset() {
            stack.clear();
            for (int i = ingredients.size() - 1; i >= 0; i--) {
                stack.push(new SearchFrame(i, db.rootBranch));
            }
        }
    }
}
