package com.gregtechceu.gtceu.api.recipe.modifier;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;

import net.minecraft.MethodsReturnNonnullByDefault;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.*;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@AllArgsConstructor
public class ParallelLogic {

    @NotNull
    public static Pair<GTRecipe, Integer> applyParallel(MetaMachine machine, @NotNull GTRecipe recipe,
                                                        int parallelLimit, boolean modifyDuration) {
        if (machine instanceof IRecipeLogicMachine rlm) {
            return doParallelRecipes(recipe, rlm, parallelLimit, modifyDuration);
        }
        return Pair.of(recipe, 1);
    }

    /**
     * @param recipe         The recipe
     * @param holder         The inventories
     * @param parallelAmount hard cap on the amount returned
     * @return returns the amount of possible time a recipe can be made from a given input inventory
     */
    public static int getMaxRecipeMultiplier(@NotNull GTRecipe recipe, @NotNull IRecipeCapabilityHolder holder,
                                             int parallelAmount) {
        IntSet multipliers = new IntOpenHashSet();

        // non-tick inputs.
        for (RecipeCapability<?> cap : recipe.inputs.keySet()) {
            if (cap.doMatchInRecipe()) {
                // Find the maximum number of recipes that can be performed from the contents of the input inventories
                multipliers.add(cap.getMaxParallelRatio(holder, recipe, parallelAmount));
            }
        }

        // tick inputs.
        for (RecipeCapability<?> cap : recipe.tickInputs.keySet()) {
            if (cap.doMatchInRecipe()) {
                // Find the maximum number of recipes that can be performed from the contents of the input inventories
                multipliers.add(cap.getMaxParallelRatio(holder, recipe, parallelAmount));
            }
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
        for (RecipeCapability<?> cap : recipe.outputs.keySet()) {
            modifiedParallelAmounts.put(cap, Integer.MAX_VALUE);
            if (!canVoid.test(cap)) {
                canVoidAll = false;
            }
        }
        for (RecipeCapability<?> cap : recipe.tickOutputs.keySet()) {
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

        for (RecipeCapability<?> cap : recipe.outputs.keySet()) {
            if (!cap.doMatchInRecipe()) {
                continue;
            }
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
        for (RecipeCapability<?> cap : recipe.tickOutputs.keySet()) {
            if (!cap.doMatchInRecipe()) {
                continue;
            }
            // Check both normal item outputs and chanced item outputs
            if (!recipe.getTickOutputContents(cap).isEmpty()) {
                boolean voiding = canVoid.test(cap);
                // If we are voiding items, reset the item limit to the maximum number of parallels
                if (voiding) {
                    if (modifiedParallelAmounts.containsKey(cap)) {
                        modifiedParallelAmounts.put(cap, Math.min(modifiedParallelAmounts.getInt(cap), parallelAmount));
                    } else {
                        modifiedParallelAmounts.put(cap, parallelAmount);
                    }
                } else {
                    if (modifiedParallelAmounts.containsKey(cap)) {
                        modifiedParallelAmounts.put(cap, Math.min(modifiedParallelAmounts.getInt(cap),
                                cap.limitParallel(recipe, holder, parallelAmount)));
                    } else {
                        modifiedParallelAmounts.put(cap, cap.limitParallel(recipe, holder, parallelAmount));
                    }
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
    @NotNull
    public static Pair<GTRecipe, Integer> doParallelRecipes(@NotNull GTRecipe currentRecipe,
                                                            @NotNull IRecipeLogicMachine machine,
                                                            int parallelAmount, boolean modifyDuration) {
        // First check if we are limited by recipe inputs. This can short circuit a lot of consecutive checking
        int multiplierByInputs = getMaxRecipeMultiplier(currentRecipe, machine, parallelAmount);
        if (multiplierByInputs == 0) {
            return Pair.of(currentRecipe, 1);
        }

        // Simulate the merging of the maximum amount of recipes that can be run with these items
        // and limit by the amount we can successfully merge
        int limitByOutput = ParallelLogic.limitByOutputMerging(currentRecipe, machine, multiplierByInputs,
                machine::canVoidRecipeOutputs);
        GTRecipe multiRecipe;
        if (limitByOutput > 0) {
            multiRecipe = currentRecipe.copy(ContentModifier.multiplier(limitByOutput), modifyDuration);
            multiRecipe.parallels *= limitByOutput;
            return Pair.of(multiRecipe, limitByOutput);
        }

        return Pair.of(currentRecipe, limitByOutput);
    }
}
