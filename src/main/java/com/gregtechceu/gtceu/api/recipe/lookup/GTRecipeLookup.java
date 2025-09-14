package com.gregtechceu.gtceu.api.recipe.lookup;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.MapIngredientTypeManager;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.item.armor.PowerlessJetpack;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class GTRecipeLookup {

    private final GTRecipeType recipeType;

    @Getter
    private final Branch lookup = new Branch();

    private static final WeakHashMap<AbstractMapIngredient, WeakReference<AbstractMapIngredient>> ingredientRoot = new WeakHashMap<>();

    /**
     * Finds a GTRecipe matching the Fluid and/or ItemStack Inputs in the holder.
     *
     * @return the GTRecipe it has found or null for no matching GTRecipe
     */
    @Nullable
    public GTRecipe findRecipe(final IRecipeCapabilityHolder holder) {
        return find(holder, recipe -> RecipeHelper.matchRecipe(holder, recipe).isSuccess());
    }

    /**
     * Prepares Items and Fluids for use in recipe search
     *
     * @param holder the recipe holder (usually machine) to prepare
     * @return a List of Lists of AbstractMapIngredients used for finding recipes
     */
    @Nullable
    protected List<List<AbstractMapIngredient>> prepareRecipeFind(@NotNull IRecipeCapabilityHolder holder) {
        // First, check if items and fluids are valid.
        int totalSize = 0;
        List<RecipeHandlerList> handlers = holder.getCapabilitiesForIO(IO.IN);

        for (var handler : handlers) {
            for (var entries : handler.getHandlerMap().entrySet()) {
                int size = 0;
                if (!entries.getKey().isRecipeSearchFilter()) {
                    continue;
                }
                for (IRecipeHandler<?> entry : entries.getValue()) {
                    if (entry.getSize() != -1) {
                        size += entry.getSize();
                    }
                }
                if (size == Integer.MAX_VALUE) {
                    return null;
                }
                totalSize += size;
            }
        }

        if (totalSize == 0) {
            return null;
        }

        // Build input.
        List<List<AbstractMapIngredient>> list = new ObjectArrayList<>(totalSize);
        list.addAll(fromHolder(holder));

        // nothing was added, so return nothing
        if (list.isEmpty()) return null;
        return list;
    }

    /**
     * Finds a recipe using Items and Fluids.
     *
     * @param holder    the holder to find recipes for.
     * @param canHandle a predicate for determining if a recipe is valid
     * @return the recipe found
     */
    @Nullable
    public GTRecipe find(@NotNull IRecipeCapabilityHolder holder, @NotNull Predicate<GTRecipe> canHandle) {
        List<List<AbstractMapIngredient>> list = prepareRecipeFind(holder);
        // couldn't build any inputs to use for search, so no recipe could be found
        if (list == null) return null;
        return recurseIngredientTreeFindRecipe(list, lookup, canHandle);
    }

    /**
     * Creates an Iterator of Recipes using Items and Fluids.
     *
     * @param holder    the holder to find recipes for.
     * @param canHandle a predicate for determining if a recipe is valid
     * @return the Recipe Iterator
     */
    @NotNull
    public RecipeIterator getRecipeIterator(@NotNull IRecipeCapabilityHolder holder,
                                            @NotNull Predicate<GTRecipe> canHandle) {
        List<List<AbstractMapIngredient>> list = prepareRecipeFind(holder);
        return new RecipeIterator(this.recipeType, list, canHandle);
    }

    /**
     * Builds a list of unique ItemStacks from the given Collection of ItemStacks.
     * Used to reduce the number inputs, if for example there is more than one of the same input,
     * pack them into one.
     * This uses a strict comparison, so it will not pack the same item with different NBT tags,
     * to allow the presence of, for example, more than one configured circuit in the input.
     *
     * @param inputs The Collection of GTRecipeInputs.
     * @return an array of unique itemstacks.
     */
    @NotNull
    public static ItemStack[] uniqueItems(@NotNull Collection<ItemStack> inputs) {
        int index = 0;
        ItemStack[] uniqueItems = new ItemStack[inputs.size()];
        main:
        for (ItemStack input : inputs) {
            if (input.isEmpty()) {
                continue;
            }
            if (index > 0) {
                for (ItemStack unique : uniqueItems) {
                    if (unique == null) break;
                    else if (ItemStack.isSameItemSameTags(input, unique)) {
                        continue main;
                    }
                }
            }
            uniqueItems[index++] = input;
        }
        if (index == uniqueItems.length) {
            return uniqueItems;
        }
        ItemStack[] retUniqueItems = new ItemStack[index];
        System.arraycopy(uniqueItems, 0, retUniqueItems, 0, index);
        return retUniqueItems;
    }

    /**
     * Recursively finds a recipe, top level.
     *
     * @param ingredients the ingredients part
     * @param branchRoot  the root branch to search from.
     * @param canHandle   if the found recipe is valid
     * @return a recipe
     */
    @Nullable
    public GTRecipe recurseIngredientTreeFindRecipe(@NotNull List<List<AbstractMapIngredient>> ingredients,
                                                    @NotNull Branch branchRoot,
                                                    @NotNull Predicate<GTRecipe> canHandle) {
        // Try each ingredient as a starting point, adding it to the skip-list.
        // The skip-list is a packed long, where each 1 bit represents an index to skip
        for (int i = 0; i < ingredients.size(); i++) {
            BitSet skipSet = new BitSet();
            skipSet.set(i);
            GTRecipe r = recurseIngredientTreeFindRecipe(ingredients, branchRoot, canHandle, i, 0, skipSet);
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    /**
     * Recursively finds a recipe
     *
     * @param ingredients the ingredients part
     * @param branchMap   the current branch of the tree
     * @param canHandle   predicate to test found recipe.
     * @param index       the index of the wrapper to get
     * @param count       how deep we are in recursion, < ingredients.length
     * @param skip        bitmap of ingredients to skip, i.e. which ingredients are already used in the recursion.
     * @return a recipe
     */
    @Nullable
    public GTRecipe recurseIngredientTreeFindRecipe(@NotNull List<List<AbstractMapIngredient>> ingredients,
                                                    @NotNull Branch branchMap, @NotNull Predicate<GTRecipe> canHandle,
                                                    int index, int count, BitSet skip) {
        // exhausted all the ingredients, and didn't find anything
        if (count == ingredients.size()) return null;

        // Iterate over current level of nodes.
        for (AbstractMapIngredient obj : ingredients.get(index)) {
            // determine the root nodes
            Map<AbstractMapIngredient, Either<GTRecipe, Branch>> targetMap = determineRootNodes(obj, branchMap);

            Either<GTRecipe, Branch> result = targetMap.get(obj);
            if (result != null) {
                // if there is a recipe (left mapping), return it immediately as found, if it can be handled
                // Otherwise, recurse and go to the next branch.
                GTRecipe r = result.map(potentialRecipe -> canHandle.test(potentialRecipe) ? potentialRecipe : null,
                        potentialBranch -> diveIngredientTreeFindRecipe(ingredients, potentialBranch, canHandle, index,
                                count, skip));
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    /**
     * Recursively finds a recipe
     *
     * @param ingredients  the ingredients part
     * @param map          the current branch of the tree
     * @param canHandle    predicate to test found recipe.
     * @param currentIndex the index of the wrapper to get
     * @param count        how deep we are in recursion, < ingredients.length
     * @param skip         bitmap of ingredients to skip, i.e. which ingredients are already used in the recursion.
     * @return a recipe
     */
    @Nullable
    private GTRecipe diveIngredientTreeFindRecipe(@NotNull List<List<AbstractMapIngredient>> ingredients,
                                                  @NotNull Branch map,
                                                  @NotNull Predicate<GTRecipe> canHandle, int currentIndex, int count,
                                                  BitSet skip) {
        // We loop around ingredients.size() if we reach the end.
        // only end when all ingredients are exhausted, or a recipe is found
        int i = (currentIndex + 1) % ingredients.size();
        while (i != currentIndex) {
            // Have we already used this ingredient? If so, skip this one.
            if (!(skip.get(i))) {
                // Recursive call
                // Increase the count, so the recursion can terminate if needed (ingredients is exhausted)
                // Append the current index to the skip list
                BitSet copy = (BitSet) skip.clone();
                copy.set(i);
                GTRecipe found = recurseIngredientTreeFindRecipe(ingredients, map, canHandle, i, count + 1,
                        copy);
                if (found != null) {
                    return found;
                }
            }
            // increment the index if the current index is skipped, or the recipe is not found
            i = (i + 1) % ingredients.size();
        }
        return null;
    }

    /**
     * Exhaustively gathers all recipes that can be crafted with the given ingredients, into a Set.
     *
     * @return a Set of recipes that can be crafted with the given ingredients
     */
    @Nullable
    public Set<GTRecipe> findRecipeCollisions(IRecipeCapabilityHolder holder) {
        List<List<AbstractMapIngredient>> list = prepareRecipeFind(holder);
        if (list == null) return null;
        Set<GTRecipe> collidingRecipes = new ObjectOpenHashSet<>();
        recurseIngredientTreeFindRecipeCollisions(list, lookup, collidingRecipes);
        return collidingRecipes;
    }

    /**
     * @param ingredients      the ingredients to search with
     * @param branchRoot       the root branch to start searching from
     * @param collidingRecipes the list to store recipe collisions
     */
    private void recurseIngredientTreeFindRecipeCollisions(@NotNull List<List<AbstractMapIngredient>> ingredients,
                                                           @NotNull Branch branchRoot,
                                                           @NotNull Set<GTRecipe> collidingRecipes) {
        // Try each ingredient as a starting point, adding it to the skip-list.
        // The skip-list is a packed long, where each 1 bit represents an index to skip
        for (int i = 0; i < ingredients.size(); i++) {
            recurseIngredientTreeFindRecipeCollisions(ingredients, branchRoot, i, 0, (1L << i), collidingRecipes);
        }
    }

    /**
     * Recursively finds all colliding recipes
     *
     * @param ingredients      the ingredients part
     * @param branchMap        the current branch of the tree
     * @param index            the index of the wrapper to get
     * @param count            how deep we are in recursion, < ingredients.length
     * @param skip             bitmap of ingredients to skip, i.e. which ingredients are already used in the recursion.
     * @param collidingRecipes the set to store the recipes in
     */
    @Nullable
    private GTRecipe recurseIngredientTreeFindRecipeCollisions(@NotNull List<List<AbstractMapIngredient>> ingredients,
                                                               @NotNull Branch branchMap, int index, int count,
                                                               long skip,
                                                               @NotNull Set<GTRecipe> collidingRecipes) {
        // exhausted all the ingredients, and didn't find anything
        if (count == ingredients.size()) return null;

        List<AbstractMapIngredient> wr = ingredients.get(index);
        // Iterate over current level of nodes.
        for (AbstractMapIngredient obj : wr) {
            // determine the root nodes
            Map<AbstractMapIngredient, Either<GTRecipe, Branch>> targetMap = determineRootNodes(obj, branchMap);

            Either<GTRecipe, Branch> result = targetMap.get(obj);
            if (result != null) {
                // if there is a recipe (left mapping), return it immediately as found
                // Otherwise, recurse and go to the next branch.
                GTRecipe r = result.map(recipe -> recipe,
                        right -> diveIngredientTreeFindRecipeCollisions(ingredients, right, index, count, skip,
                                collidingRecipes));
                if (r != null) {
                    collidingRecipes.add(r);
                }
            }
        }
        return null;
    }

    /**
     * Recursively finds a recipe
     *
     * @param ingredients      the ingredients part
     * @param map              the current branch of the tree
     * @param currentIndex     the index of the wrapper to get
     * @param count            how deep we are in recursion, < ingredients.length
     * @param skip             bitmap of ingredients to skip, i.e. which ingredients are already used in the recursion.
     * @param collidingRecipes the set to store the recipes in
     * @return a recipe
     */
    @Nullable
    private GTRecipe diveIngredientTreeFindRecipeCollisions(@NotNull List<List<AbstractMapIngredient>> ingredients,
                                                            @NotNull Branch map, int currentIndex, int count, long skip,
                                                            @NotNull Set<GTRecipe> collidingRecipes) {
        // We loop around ingredients.size() if we reach the end.
        // only end when all ingredients are exhausted, or a recipe is found
        int i = (currentIndex + 1) % ingredients.size();
        while (i != currentIndex) {
            // Have we already used this ingredient? If so, skip this one.
            if (((skip & (1L << i)) == 0)) {
                // Recursive call
                // Increase the count, so the recursion can terminate if needed (ingredients is exhausted)
                // Append the current index to the skip list
                GTRecipe r = recurseIngredientTreeFindRecipeCollisions(ingredients, map, i, count + 1, skip | (1L << i),
                        collidingRecipes);
                if (r != null) {
                    return r;
                }
            }
            // increment the index if the current index is skipped, or the recipe is not found
            i = (i + 1) % ingredients.size();
        }
        return null;
    }

    /**
     * Retrieves a cached ingredient, or inserts a default one
     *
     * @param list        the list to append to
     * @param ingredients the ingredient to use as a default value, if not cached
     * @param cache       the ingredient root to retrieve from
     */
    protected static void retrieveCachedIngredient(@NotNull List<List<AbstractMapIngredient>> list,
                                                   @NotNull List<AbstractMapIngredient> ingredients,
                                                   @NotNull WeakHashMap<AbstractMapIngredient, WeakReference<AbstractMapIngredient>> cache) {
        for (int i = 0; i < ingredients.size(); i++) {
            AbstractMapIngredient mappedIngredient = ingredients.get(i);
            // attempt to use the cached value if possible, otherwise cache for the next time
            WeakReference<AbstractMapIngredient> cached = cache.get(mappedIngredient);
            if (cached != null && cached.get() != null) {
                ingredients.set(i, cached.get());
            } else {
                cache.put(mappedIngredient, new WeakReference<>(mappedIngredient));
            }
        }
        list.add(ingredients);
    }

    /**
     * Converts a GTRecipe's {@link RecipeCapability RecipeCapabilities} into
     * a list of {@link AbstractMapIngredient AbstractMapIngredients}
     *
     * @param recipe the recipe to use
     * @return a list of all the AbstractMapIngredients comprising the recipe
     */
    @NotNull
    protected List<List<AbstractMapIngredient>> fromRecipe(@NotNull GTRecipe recipe) {
        int initialCapacity = (recipe.inputs.size() + recipe.tickInputs.size()) * 2;
        List<List<AbstractMapIngredient>> list = new ObjectArrayList<>(initialCapacity);
        recipe.inputs.forEach(processCapabilityIngredients(list));
        recipe.tickInputs.forEach(processCapabilityIngredients(list));
        return list;
    }

    // spotless:off
    protected BiConsumer<RecipeCapability<?>, List<Content>> processCapabilityIngredients(List<List<AbstractMapIngredient>> list) {
        return (cap, contents) -> {
            if (cap.isRecipeSearchFilter() && !contents.isEmpty()) {
                List<Object> ingredients = new ArrayList<>();
                for (Content content : contents) {
                    ingredients.add(content.getContent());
                }
                ingredients = cap.compressIngredients(ingredients);
                for (Object ingredient : ingredients) {
                    // use the cached ingredient, if possible
                    retrieveCachedIngredient(list, MapIngredientTypeManager.getFrom(ingredient, cap), ingredientRoot);
                }
            }
        };
    }
    // spotless:on

    /**
     * Converts a Recipe Capability holder's handlers into
     * a list of {@link AbstractMapIngredient AbstractMapIngredients}
     *
     * @param holder the capability holder to query handlers from
     * @return a list of all the AbstractMapIngredients in the handlers
     */
    @NotNull
    protected List<List<AbstractMapIngredient>> fromHolder(@NotNull IRecipeCapabilityHolder holder) {
        var handlerMap = holder.getCapabilitiesFlat().getOrDefault(IO.IN, Collections.emptyMap());
        // the initial capacity is a "feel-good" value because it's faster to just grow the list
        // than to calculate an accurate value.
        List<List<AbstractMapIngredient>> list = new ObjectArrayList<>(handlerMap.size() * 8);
        for (var entry : handlerMap.entrySet()) {
            var cap = entry.getKey();
            var handlers = entry.getValue();
            if (!cap.isRecipeSearchFilter()) continue;
            for (var handler : handlers) {
                var compressed = cap.compressIngredients(handler.getContents());
                for (var ingredient : compressed) {
                    list.add(MapIngredientTypeManager.getFrom(ingredient, cap));
                }
            }
        }
        return list;
    }

    /**
     * Removes all recipes.
     */
    @ApiStatus.Internal
    public void removeAllRecipes() {
        this.lookup.getNodes().clear();
        this.lookup.getSpecialNodes().clear();
        this.recipeType.getCategoryMap().clear();
    }

    /**
     * Compiles a recipe and adds it to the ingredient tree
     *
     * @param recipe the recipe to compile
     * @return if the recipe was successfully compiled
     */
    public boolean addRecipe(GTRecipe recipe) {
        if (recipe == null) {
            return false;
        }

        // Add combustion fuels to the Powerless Jetpack
        if (recipe.getType() == GTRecipeTypes.COMBUSTION_GENERATOR_FUELS) {
            Content content = recipe.getInputContents(FluidRecipeCapability.CAP).get(0);
            FluidIngredient fluid = FluidRecipeCapability.CAP.of(content.content);
            PowerlessJetpack.FUELS.putIfAbsent(fluid, recipe.duration);
        }
        List<List<AbstractMapIngredient>> items = fromRecipe(recipe);
        if (recurseIngredientTreeAdd(recipe, items, lookup, 0, 0)) {
            recipe.recipeCategory.addRecipe(recipe);
            return true;
        }
        return false;
    }

    /**
     * Adds a recipe to the map. (recursive part)
     *
     * @param recipe      the recipe to add.
     * @param ingredients list of input ingredients representing the recipe.
     * @param branchMap   the current branch in the recursion.
     * @param index       where in the ingredients list we are.
     * @param count       how many branches were added already.
     */
    private boolean recurseIngredientTreeAdd(@NotNull GTRecipe recipe,
                                             @NotNull List<List<AbstractMapIngredient>> ingredients,
                                             @NotNull Branch branchMap, int index, int count) {
        if (count >= ingredients.size()) return true;
        if (index >= ingredients.size()) {
            throw new RuntimeException("Index out of bounds for recurseItemTreeAdd, should not happen");
        }
        // Loop through NUMBER_OF_INGREDIENTS times.

        // the current contents to be added to a node in the branch
        final List<AbstractMapIngredient> current = ingredients.get(index);
        final Branch branchRight = new Branch();
        Either<GTRecipe, Branch> r;

        // for every ingredient, add it to a node
        for (AbstractMapIngredient obj : current) {
            // determine the root nodes
            Map<AbstractMapIngredient, Either<GTRecipe, Branch>> targetMap = determineRootNodes(obj, branchMap);

            // Either add the recipe or create a branch.
            r = targetMap.compute(obj, (k, v) -> {
                if (count == ingredients.size() - 1) {
                    // handle very last ingredient
                    if (v != null) {
                        // handle the existing branch
                        if (v.left().isEmpty() || v.left().get() != recipe) {
                            // the recipe already there was not the one being added, so there is a conflict
                            if (ConfigHolder.INSTANCE.dev.debug || GTCEu.isDev()) {
                                GTCEu.LOGGER.warn(
                                        "Recipe duplicate or conflict found in GTRecipeType {} and was not added. See next lines for details",
                                        BuiltInRegistries.RECIPE_TYPE.getKey(this.recipeType));

                                GTCEu.LOGGER.warn("Attempted to add GTRecipe: {}", recipe.getId());

                                if (v.left().isPresent()) {
                                    GTCEu.LOGGER.warn("Which conflicts with: {}", v.left().get().getId());
                                } else {
                                    GTCEu.LOGGER.warn("Could not find exact duplicate/conflict.");
                                }
                            }
                        }
                        // Return the existing recipe, even on conflicts.
                        // If there was no conflict but a recipe was still present, it was added on an earlier recurse,
                        // and this will carry the result further back in the call stack
                        return v;
                    } else {
                        // nothing exists for this path, so end with the recipe
                        return Either.left(recipe);
                    }
                } else if (v == null) {
                    // no existing ingredient is present, so use the new one
                    return Either.right(branchRight);
                }
                // there is an existing ingredient here already, so use it
                return v;
            });

            // left branches are always either empty or contain recipes.
            // If there's a recipe present, the addition is finished for this ingredient
            if (r.left().isPresent()) {
                if (r.left().get() == recipe) {
                    // Cannot return here, since each ingredient to add is a separate path to the recipe
                    continue;
                } else {
                    // exit if a different recipe is already present for this path
                    return false;
                }
            }

            // recursive part: apply the addition for the next ingredient in the list, for the right branch.
            // the right branch only contains ingredients, or is empty when the left branch is present
            boolean addedNextBranch = r.right()
                    .filter(m -> recurseIngredientTreeAdd(recipe, ingredients, m, (index + 1) % ingredients.size(),
                            count + 1))
                    .isPresent();

            if (!addedNextBranch) {
                // failed to add the next branch, so undo any made changes
                if (count == ingredients.size() - 1) {
                    // was the final ingredient, so the mapping of it to a recipe needs to be removed
                    targetMap.remove(obj);
                } else {
                    // was a regular ingredient
                    if (targetMap.get(obj).right().isPresent()) {
                        // if something was put into the map
                        if (targetMap.get(obj).right().get().isEmptyBranch()) {
                            // if what was put was empty (invalid), remove it
                            targetMap.remove(obj);
                        }
                    }
                }
                // because a branch addition failure happened, fail the recipe addition for this step
                return false;
            }
        }
        // recipe addition was successful
        return true;
    }

    /**
     * Determine the correct root nodes for an ingredient
     *
     * @param ingredient the ingredient to check
     * @param branchMap  the branch containing the nodes
     * @return the correct nodes for the ingredient
     */
    @NotNull
    protected static Map<AbstractMapIngredient, Either<GTRecipe, Branch>> determineRootNodes(@NotNull AbstractMapIngredient ingredient,
                                                                                             @NotNull Branch branchMap) {
        return ingredient.isSpecialIngredient() ? branchMap.getSpecialNodes() : branchMap.getNodes();
    }
}
