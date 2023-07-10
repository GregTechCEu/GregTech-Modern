package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import it.unimi.dsi.fastutil.longs.LongIntMutablePair;
import it.unimi.dsi.fastutil.longs.LongIntPair;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2023/2/26
 * @implNote RecipeHelper
 */
public class RecipeHelper {

    public static long getInputEUt(GTRecipe recipe) {
        return recipe.getTickInputContents(EURecipeCapability.CAP).stream()
                .map(Content::getContent)
                .mapToLong(EURecipeCapability.CAP::of)
                .sum();
    }

    public static long getOutputEUt(GTRecipe recipe) {
        return recipe.getTickOutputContents(EURecipeCapability.CAP).stream()
                .map(Content::getContent)
                .mapToLong(EURecipeCapability.CAP::of)
                .sum();
    }

    public static void setInputEUt(GTRecipe recipe, long eut) {
        recipe.getTickInputContents(EURecipeCapability.CAP).forEach(c -> c.content = eut);
    }

    public static void setOutputEUt(GTRecipe recipe, long eut) {
        recipe.getTickOutputContents(EURecipeCapability.CAP).forEach(c -> c.content = eut);
    }

    public static int getRecipeEUtTier(GTRecipe recipe) {
        long EUt = getInputEUt(recipe);
        if (EUt == 0) {
            EUt = getOutputEUt(recipe);
        }
        return GTUtil.getTierByVoltage(EUt);
    }

    /**
     * Calculates the overclocked Recipe's final duration and EU/t
     *
     * @param recipe the recipe to run
     * @return a new recipe
     */
    public static GTRecipe applyOverclock(OverclockingLogic logic, @Nonnull GTRecipe recipe, long maxOverclockVoltage) {
        long EUt = getInputEUt(recipe);
        if (EUt > 0) {
            var overclockResult = performOverclocking(logic, recipe, EUt, maxOverclockVoltage);
            if (overclockResult.leftLong() != EUt || recipe.duration != overclockResult.rightInt()) {
                recipe = recipe.copy();
                recipe.duration = overclockResult.rightInt();
                for (Content content : recipe.getTickInputContents(EURecipeCapability.CAP)) {
                    content.content = overclockResult.leftLong();
                }
            }
        }
        EUt = getOutputEUt(recipe);
        if (EUt > 0) {
            var overclockResult = performOverclocking(logic, recipe, EUt, maxOverclockVoltage);
            if (overclockResult.leftLong() != EUt || recipe.duration != overclockResult.rightInt()) {
                recipe = recipe.copy();
                recipe.duration = overclockResult.rightInt();
                for (Content content : recipe.getTickOutputContents(EURecipeCapability.CAP)) {
                    content.content = overclockResult.leftLong();
                }
            }
        }
        return recipe;
    }

    /**
     * Determines the maximum number of overclocks that can be performed for a recipe.
     * Then performs overclocking on the Recipe.
     *
     * @param recipe the recipe to overclock
     * @return an int array of {OverclockedEUt, OverclockedDuration}
     */
    private static LongIntPair performOverclocking(OverclockingLogic logic, @Nonnull GTRecipe recipe, long EUt, long maxOverclockVoltage) {
        int recipeTier = GTUtil.getTierByVoltage(EUt);
        int maximumTier = logic.getOverclockForTier(maxOverclockVoltage);
        // The maximum number of overclocks is determined by the difference between the tier the recipe is running at,
        // and the maximum tier that the machine can overclock to.
        int numberOfOCs = maximumTier - recipeTier;
        if (recipeTier == GTValues.ULV) numberOfOCs--; // no ULV overclocking

        // cannot overclock, so return the starting values
        if (numberOfOCs <= 0) return LongIntMutablePair.of(EUt, recipe.duration);

        return logic.getLogic().runOverclockingLogic(recipe, EUt, maxOverclockVoltage, recipe.duration, numberOfOCs);
    }

}
