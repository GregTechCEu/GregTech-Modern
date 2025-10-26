package com.gregtechceu.gtceu.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Internal class handling adding recipes to GT's lookup system.
 * <p>
 * Intended for use by {@link com.gregtechceu.gtceu.core.mixins.RecipeManagerMixin} and
 * {@link com.gregtechceu.gtceu.integration.kjs.GregTechKubeJSPlugin}
 */
@ApiStatus.Internal
public final class RecipeManagerHandler {

    /**
     * Adds proxy recipes to an {@link GTRecipeType}'s {@link GTRecipeLookup} and adds them to a list.
     *
     * @param recipesByID  the recipes stored by their ID
     * @param gtRecipeType the recipe type to add the recipes to, which owns the proxy recipes
     * @param proxyRecipes the list of proxy recipes to populate
     */
    public static void addProxyRecipesToLookup(@NotNull Map<ResourceLocation, Recipe<?>> recipesByID,
                                               @NotNull GTRecipeType gtRecipeType, @NotNull RecipeType<?> proxyType,
                                               @NotNull List<GTRecipe> proxyRecipes) {
        var lookup = gtRecipeType.getLookup();
        proxyRecipes.clear();
        recipesByID.forEach((id, recipe) -> {
            if (recipe.getType() != proxyType) {
                // do not add recipes of incompatible type
                return;
            }
            GTRecipe gtRecipe = gtRecipeType.toGTrecipe(id, recipe);
            proxyRecipes.add(gtRecipe);
            lookup.addRecipe(gtRecipe);
        });
    }

    /**
     * Adds recipes to an {@link GTRecipeType}'s {@link GTRecipeLookup}
     *
     * @param recipesByID  the recipes stored by their ID
     * @param gtRecipeType the recipe type to add recipes to
     */
    public static void addRecipesToLookup(@NotNull Map<ResourceLocation, Recipe<?>> recipesByID,
                                          @NotNull GTRecipeType gtRecipeType) {
        var lookup = gtRecipeType.getLookup();
        for (var r : recipesByID.values()) {
            if (r.getType() != gtRecipeType) {
                // do not add recipes of incompatible type
                continue;
            }
            if (r instanceof GTRecipe recipe) {
                lookup.addRecipe(recipe);
            }
        }
    }
}
