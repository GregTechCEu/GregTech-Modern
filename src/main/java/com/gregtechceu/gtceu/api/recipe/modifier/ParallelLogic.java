package com.gregtechceu.gtceu.api.recipe.modifier;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;

import net.minecraft.MethodsReturnNonnullByDefault;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ParallelLogic {

    /**
     * Calculates the maximum parallel amount that can be done for the given machine and recipe, up to the passed limit
     *
     * @param machine       machine to test against
     * @param recipe        recipe to test with
     * @param parallelLimit hard upper limit of parallels that can be done
     * @return The number of possible parallels, 0 if the recipe cannot be done
     */
    public static int getParallelAmount(MetaMachine machine, GTRecipe recipe, int parallelLimit) {
        if (parallelLimit <= 1) return parallelLimit;
        if (!(machine instanceof IRecipeLogicMachine rlm)) return 1;
        // First check if we are limited by recipe inputs. This can short circuit a lot of consecutive checking
        int maxInputMultiplier = getMaxByInput(rlm, recipe, parallelLimit, Collections.emptyList());
        if (maxInputMultiplier == 0) return 0;

        // Simulate the merging of the maximum amount of recipes that can be run with these items
        // and limit by the amount we can successfully merge
        return limitByOutputMerging(rlm, recipe, maxInputMultiplier, rlm::canVoidRecipeOutputs,
                Collections.emptyList());
    }

    /**
     * @param holder        The inventories
     * @param recipe        The recipe
     * @param parallelLimit hard cap on the amount returned
     * @param capsToSkip    the capabilities to skip parallel testing
     * @return returns the amount of possible time a recipe can be made from a given input inventory
     */
    public static int getMaxByInput(IRecipeCapabilityHolder holder, GTRecipe recipe, int parallelLimit,
                                    List<RecipeCapability<?>> capsToSkip) {
        int minimum = Integer.MAX_VALUE;

        // non-tick inputs.
        for (RecipeCapability<?> cap : recipe.inputs.keySet()) {
            if (cap.doMatchInRecipe() && !capsToSkip.contains(cap)) {
                // Find the maximum number of recipes that can be performed from the contents of the input inventories
                minimum = Math.min(minimum, cap.getMaxParallelByInput(holder, recipe, parallelLimit, false));
            }
        }

        // tick inputs.
        for (RecipeCapability<?> cap : recipe.tickInputs.keySet()) {
            if (cap.doMatchInRecipe() && !capsToSkip.contains(cap)) {
                // Find the maximum number of recipes that can be performed from the contents of the input inventories
                minimum = Math.min(minimum, cap.getMaxParallelByInput(holder, recipe, parallelLimit, true));
            }
        }
        if (minimum == Integer.MAX_VALUE) return 0;
        return minimum;
    }

    /**
     * @param holder        the inventories
     * @param recipe        The recipe
     * @param parallelLimit the maximum allowed amount
     * @param canVoid       predicate for what parallel limits should be ignored
     * @param capsToSkip    the capabilities to skip parallel testing
     * @return returns the amount of recipes that can be merged successfully into a given output inventory
     */
    public static int limitByOutputMerging(IRecipeCapabilityHolder holder, GTRecipe recipe, int parallelLimit,
                                           Predicate<RecipeCapability<?>> canVoid,
                                           List<RecipeCapability<?>> capsToSkip) {
        int max = parallelLimit;
        for (RecipeCapability<?> cap : recipe.outputs.keySet()) {
            if (canVoid.test(cap) || !cap.doMatchInRecipe() || capsToSkip.contains(cap)) {
                continue;
            }
            // Check both normal item outputs and chanced item outputs
            if (!recipe.getOutputContents(cap).isEmpty()) {
                int limit = cap.limitMaxParallelByOutput(holder, recipe, parallelLimit, false);
                // If we are not voiding, and cannot fit any items, return 0
                if (limit == 0) {
                    return 0;
                }
                max = Math.min(max, limit);
            }
        }
        for (RecipeCapability<?> cap : recipe.tickOutputs.keySet()) {
            if (canVoid.test(cap) || !cap.doMatchInRecipe() || capsToSkip.contains(cap)) {
                continue;
            }
            // Check both normal item outputs and chanced item outputs
            if (!recipe.getTickOutputContents(cap).isEmpty()) {
                int limit = cap.limitMaxParallelByOutput(holder, recipe, parallelLimit, true);
                // If we are not voiding, and cannot fit any items, return 0
                if (limit == 0) {
                    return 0;
                }
                max = Math.min(max, limit);
            }
        }
        return max;
    }

    /**
     * Calculates the maximum parallel amount that can be done for the given machine and recipe, up to the passed limit
     *
     * @param machine       machine to test against
     * @param recipe        recipe to test with
     * @param parallelLimit hard upper limit of parallels that can be done
     * @return The number of possible parallels, 0 if the recipe cannot be done
     */
    public static int getParallelAmountWithoutEU(MetaMachine machine, GTRecipe recipe, int parallelLimit) {
        if (parallelLimit <= 1) return parallelLimit;
        if (!(machine instanceof IRecipeLogicMachine rlm)) return 1;
        // First check if we are limited by recipe inputs. This can short circuit a lot of consecutive checking
        int maxInputMultiplier = getMaxByInput(rlm, recipe, parallelLimit, List.of(EURecipeCapability.CAP));
        if (maxInputMultiplier == 0) return 0;

        // Simulate the merging of the maximum amount of recipes that can be run with these items
        // and limit by the amount we can successfully merge
        return limitByOutputMerging(rlm, recipe, maxInputMultiplier, rlm::canVoidRecipeOutputs,
                List.of(EURecipeCapability.CAP));
    }

    /**
     * Binary-search-like approach to find the maximum amount that can be inserted
     *
     * @param mergedAll     if the merge was successful.
     *                      If true sets {@code minMultiplier} to the as the current multiplier
     *                      then sets {@code multiplier} to the sum of the mean difference between
     *                      {@code multiplier} and {@code maxMultiplier} plus the remainder of the division, if any,
     *                      and itself
     *                      If false, sets {@code maxMultiplier} as the current multiplier, then sets {@code multiplier}
     *                      to half of its value limited it to no less or than the value of {@code minMultiplier}
     * @param minMultiplier the last known multiplier what was fully merged
     * @param multiplier    the current multiplier
     * @param maxMultiplier the last know multiplier that resulted in simulation failure
     * @return an array consisting of the last known multiplier, new multiplier to be attempted and
     *         the last know multiplier that resulted in failure
     */
    public static int[] adjustMultiplier(boolean mergedAll, int minMultiplier, int multiplier, int maxMultiplier) {
        if (mergedAll) {
            minMultiplier = multiplier;
            int remainder = (maxMultiplier - multiplier) % 2;
            multiplier = multiplier + remainder + (maxMultiplier - multiplier) / 2;
        } else {
            maxMultiplier = multiplier;
            multiplier = (multiplier + minMultiplier) / 2;
        }
        if (maxMultiplier - minMultiplier <= 1) {
            multiplier = maxMultiplier = minMultiplier;
        }
        return new int[] { minMultiplier, multiplier, maxMultiplier };
    }

    /**
     * Fast parallel, the parallel amount is always the 2 times the divisor of parallelLimit.
     *
     * @param machine       recipe holder
     * @param recipe        current recipe
     * @param parallelLimit max parallel limited
     * @return Returns the number of parallels that can be done (fast calc)
     */
    public static int getParallelAmountFast(MetaMachine machine, @NotNull GTRecipe recipe, int parallelLimit) {
        if (parallelLimit <= 1) return parallelLimit;
        if (!(machine instanceof IRecipeCapabilityHolder holder)) return 1;

        while (parallelLimit > 0) {
            var copied = recipe.copy(ContentModifier.multiplier(parallelLimit), false);
            if (RecipeHelper.matchRecipe(holder, copied).isSuccess() &&
                    RecipeHelper.matchTickRecipe(holder, copied).isSuccess()) {
                return parallelLimit;
            }
            parallelLimit /= 2;
        }
        return 1;
    }
}
