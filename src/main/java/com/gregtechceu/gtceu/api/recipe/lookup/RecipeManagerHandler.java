package com.gregtechceu.gtceu.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Internal class handling adding recipes to GT's lookup system.
 * <p>
 * Intended for use by {@link com.gregtechceu.gtceu.core.mixins.RecipeManagerLateMixin} and
 * {@link com.gregtechceu.gtceu.integration.kjs.GregTechKubeJSPlugin}
 */
@ApiStatus.Internal
public final class RecipeManagerHandler {

    /**
     * Adds proxy recipes to an {@link GTRecipeType}'s {@link RecipeAdditionHandler} and adds them to a list.
     *
     * @param recipes      the recipes stored by their ID
     * @param gtRecipeType the recipe type to add the recipes to, which owns the proxy recipes
     * @param proxyRecipes the list of proxy recipes to populate
     */
    public static void addProxyRecipesToLookup(@NotNull Collection<RecipeHolder<?>> recipes,
                                               @NotNull GTRecipeType gtRecipeType, @NotNull RecipeType<?> proxyType,
                                               @NotNull List<RecipeHolder<GTRecipe>> proxyRecipes) {
        var lookup = gtRecipeType.getAdditionHandler();
        proxyRecipes.clear();
        recipes.forEach((recipe) -> {
            if (recipe.value().getType() != proxyType) {
                // do not add recipes of incompatible type
                return;
            }
            RecipeHolder<GTRecipe> gtRecipe = gtRecipeType.toGTRecipe(recipe);
            proxyRecipes.add(gtRecipe);
            lookup.addStaging(gtRecipe.value());
        });
    }

    /**
     * Adds recipes to an {@link GTRecipeType}'s {@link RecipeAdditionHandler}
     *
     * @param recipes      the recipes stored by their ID
     * @param gtRecipeType the recipe type to add recipes to
     */
    public static void addRecipesToLookup(@NotNull Collection<RecipeHolder<?>> recipes,
                                          @NotNull GTRecipeType gtRecipeType) {
        var lookup = gtRecipeType.getAdditionHandler();
        for (RecipeHolder<?> r : recipes) {
            if (r.value().getType() != gtRecipeType) {
                // do not add recipes of incompatible type
                continue;
            }
            if (r.value() instanceof GTRecipe recipe) {
                lookup.addStaging(recipe);
            }
        }
    }
}
