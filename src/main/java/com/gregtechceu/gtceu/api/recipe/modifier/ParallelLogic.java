package com.gregtechceu.gtceu.api.recipe.modifier;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.*;
import lombok.AllArgsConstructor;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@AllArgsConstructor
public class ParallelLogic {

    @Nullable
    public static Pair<GTRecipe, Integer> applyParallel(MetaMachine machine, @Nullable GTRecipe recipe, int parallelLimit, boolean modifyDuration) {
        if (recipe == null) {
            return null;
        }
        if (machine instanceof IRecipeLogicMachine rlm) {
            return doParallelRecipes(recipe, rlm, parallelLimit, modifyDuration);
        }
        return null;
    }

    /**
     * @param recipe         The recipe
     * @param holder         The inventories
     * @param parallelAmount hard cap on the amount returned
     * @return returns the amount of possible time a recipe can be made from a given input inventory
     */
    public static int getMaxRecipeMultiplier(@NotNull GTRecipe recipe, @NotNull IRecipeCapabilityHolder holder, int parallelAmount) {
        IntSet multipliers = new IntOpenHashSet();

        for (RecipeCapability<?> cap : recipe.inputs.keySet()) {
            // Find the maximum number of recipes that can be performed from the contents of the input inventories
            multipliers.add(cap.getMaxParallelRatio(holder, recipe, parallelAmount));
        }
        if (multipliers.intStream().allMatch(value -> value == Integer.MAX_VALUE)) {
            return 0;
        }
        // Find the maximum number of recipes that can be performed from all available inputs
        return multipliers.intStream().min().orElse(0);
    }

    /**
     * @param recipe         The recipe
     * @param holder         the inventories
     * @param parallelAmount the maximum expected amount
     * @param canVoid        predicate for what parallel limits should be ignored
     * @return returns the amount of recipes that can be merged successfully into a given output inventory
     */
    public static int limitByOutputMerging(@NotNull GTRecipe recipe, @NotNull IRecipeCapabilityHolder holder,
                                           int parallelAmount, Predicate<RecipeCapability<?>> canVoid) {
        Object2IntMap<RecipeCapability<?>> modifiedParallelAmounts = new Object2IntOpenHashMap<>();
        boolean canVoidAll = true;
        for (RecipeCapability<?> cap : recipe.inputs.keySet()) {
            modifiedParallelAmounts.put(cap, Integer.MAX_VALUE);
            if (!canVoid.test(cap)) {
                canVoidAll = false;
            }
        }
        // If we are voiding everything, return the maximum number of parallels that can be performed from
        // the inputs
        if (canVoidAll) {
            return parallelAmount;
        }

        for (RecipeCapability<?> cap : recipe.inputs.keySet()) {
            // Check both normal item outputs and chanced item outputs
            if (!recipe.getOutputContents(cap).isEmpty()) {
                boolean voiding = canVoid.test(cap);
                // If we are voiding items, reset the item limit to the maximum number of parallels
                if (voiding) {
                    modifiedParallelAmounts.put(cap, parallelAmount);
                } else {
                    modifiedParallelAmounts.put(cap, cap.limitParallel(recipe, holder, parallelAmount));
                }

                // If we are not voiding, and cannot fit any items, return 0
                if (modifiedParallelAmounts.getInt(cap) == 0 && !voiding) {
                    return 0;
                }
            }
        }

        return modifiedParallelAmounts.values().intStream().min().orElse(0);
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
    public static int @NotNull [] adjustMultiplier(boolean mergedAll, int minMultiplier, int multiplier,
                                                   int maxMultiplier) {
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

    // At this point, the recipe is already trimmed according to the item and fluid output limit, so we just need to
    // take care of voiding
    @Nullable
    public static Pair<GTRecipe, Integer> doParallelRecipes(@NotNull GTRecipe currentRecipe,
                                             @NotNull IRecipeLogicMachine machine,
                                             int parallelAmount, boolean modifyDuration) {
        long maxVoltage = Long.MAX_VALUE;
        if (machine instanceof IOverclockMachine overclockMachine) {
            maxVoltage = overclockMachine.getOverclockVoltage();
        } else if (machine instanceof ITieredMachine tieredMachine) {
            maxVoltage = GTValues.V[tieredMachine.getTier()];
        }
        // First check if we are limited by recipe inputs. This can short circuit a lot of consecutive checking
        int multiplierByInputs = getMaxRecipeMultiplier(currentRecipe, machine, parallelAmount);
        if (multiplierByInputs == 0) {
            return null;
        }

        // Simulate the merging of the maximum amount of recipes that can be run with these items
        // and limit by the amount we can successfully merge
        int limitByOutput = ParallelLogic.limitByOutputMerging(currentRecipe, machine, multiplierByInputs, machine::canVoidRecipeOutputs);
        int parallelLimit = limitByOutput;

        long recipeEUt = RecipeHelper.getInputEUt(currentRecipe);
        if (recipeEUt == 0) {
            recipeEUt = RecipeHelper.getOutputEUt(currentRecipe);
        }
        if (recipeEUt != 0) {
            int limitByVoltage = Math.abs((int) (maxVoltage / recipeEUt));
            int maxParallel = Math.min(limitByVoltage, limitByOutput);
            if (maxParallel != 0) {
                // Use the minimum between the amount of recipes we can run with available inputs and amount of recipe
                // outputs that can fit
                parallelLimit = Math.min(maxParallel, multiplierByInputs);
                currentRecipe = currentRecipe.copy(ContentModifier.multiplier(parallelLimit), modifyDuration);
            }
        } else if (limitByOutput > 0) {
            currentRecipe = currentRecipe.copy(ContentModifier.multiplier(limitByOutput), modifyDuration);
        }

        return Pair.of(currentRecipe, parallelLimit);
    }
}
