package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import it.unimi.dsi.fastutil.longs.LongIntPair;
import lombok.val;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2023/7/9
 * @implNote GTRecipeModifiers
 */
public class GTRecipeModifiers {

    /**
     * Use it if machines are {@link IOverclockMachine}.
     */
    public final static Function<OverclockingLogic, BiFunction<MetaMachine, GTRecipe, GTRecipe>> ELECTRIC_OVERCLOCK = Util.memoize(overclockingLogic -> (machine, recipe) -> {
        if (machine instanceof ITieredMachine tieredMachine && RecipeHelper.getRecipeEUtTier(recipe) > tieredMachine.getTier()) {
            return null;
        }
        if (machine instanceof IOverclockMachine overclockMachine) {
            return RecipeHelper.applyOverclock(overclockingLogic, recipe, overclockMachine.getOverclockVoltage());
        }
        return recipe;
    });

    /**
     * Fast parallel, the parallel amount is always the 2 times the divisor of maxParallel。
     * @param holder recipe holder
     * @param recipe current recipe
     * @param maxParallel max parallel limited
     * @param modifyDuration should multiply the duration
     * @return modified recipe
     */
    public static GTRecipe fastParallel(IRecipeCapabilityHolder holder, @Nonnull GTRecipe recipe, int maxParallel, boolean modifyDuration) {
        while (maxParallel > 0) {
            var copied = recipe.copy(ContentModifier.multiplier(maxParallel), modifyDuration);
            if (copied.matchRecipe(holder).isSuccessed()) {
                return copied;
            }
            maxParallel /= 2;
        }
        return recipe;
    };

    /**
     * Accurate parallel, always look for the maximum parallel value within maxParallel.
     * @param machine recipe holder
     * @param recipe current recipe
     * @param maxParallel max parallel limited
     * @param modifyDuration should multiply the duration
     * @return modified recipe
     */
    public static GTRecipe accurateParallel(MetaMachine machine, @Nonnull GTRecipe recipe, int maxParallel, boolean modifyDuration) {
        if (machine instanceof IRecipeCapabilityHolder holder) {
            var parallel = tryParallel(holder, recipe, 1, maxParallel, modifyDuration);
            return parallel == null ? recipe : parallel;
        }
        return null;
    }

    @Nullable
    private static GTRecipe tryParallel(IRecipeCapabilityHolder holder, GTRecipe original, int min, int max, boolean modifyDuration) {
        if (min > max) return null;

        int mid = (min + max) / 2;

        GTRecipe copied = original.copy(ContentModifier.multiplier(mid), modifyDuration);
        if (!copied.matchRecipe(holder).isSuccessed()) {
            // tried too many
            return tryParallel(holder, original, min, mid - 1, modifyDuration);
        } else {
            // at max parallels
            if (mid == max) {
                return copied;
            }
            // matches, but try to do more
            GTRecipe tryMore = tryParallel(holder, original, mid + 1, max, modifyDuration);
            return tryMore != null ? tryMore : copied;
        }
    }

    public static GTRecipe crackerOverclock(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (machine instanceof CoilWorkableElectricMultiblockMachine coilMachine) {
            if (RecipeHelper.getRecipeEUtTier(recipe) > coilMachine.getTier()) {
                return null;
            }
            return RecipeHelper.applyOverclock(new OverclockingLogic((recipe1, recipeEUt, maxVoltage, duration, amountOC) -> {
                var pair = OverclockingLogic.NON_PERFECT_OVERCLOCK.getLogic().runOverclockingLogic(recipe, recipeEUt, maxVoltage, duration, amountOC);
                if (coilMachine.getCoilTier() > 0) {
                    var eu = pair.firstLong() * (1 - coilMachine.getCoilTier() * 0.1);
                    pair.first((long) Math.max(1, eu));
                }
                return pair;
            }), recipe, coilMachine.getMaxVoltage());
        }
        return null;
    }

    public static GTRecipe ebfOverclock(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (machine instanceof CoilWorkableElectricMultiblockMachine coilMachine) {
            val blastFurnaceTemperature = coilMachine.getCoilType().getCoilTemperature() + 100 * Math.max(0, coilMachine.getTier() - GTValues.MV);
            if (!recipe.data.contains("ebf_temp") || recipe.data.getInt("ebf_temp") > blastFurnaceTemperature) {
                return null;
            }
            if (RecipeHelper.getRecipeEUtTier(recipe) > coilMachine.getTier()) {
                return null;
            }
            return RecipeHelper.applyOverclock(new OverclockingLogic((recipe1, recipeEUt, maxVoltage, duration, amountOC) -> OverclockingLogic.heatingCoilOverclockingLogic(
                    Math.abs(recipeEUt),
                    maxVoltage,
                    duration,
                    amountOC,
                    blastFurnaceTemperature,
                    recipe.data.contains("ebf_temp") ? 0 : recipe.data.getInt("ebf_temp")
            )), recipe, coilMachine.getMaxVoltage());
        }
        return null;
    }

    public static GTRecipe pyrolyseOvenOverclock(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (machine instanceof CoilWorkableElectricMultiblockMachine coilMachine) {
            if (RecipeHelper.getRecipeEUtTier(recipe) > coilMachine.getTier()) {
                return null;
            }
            return RecipeHelper.applyOverclock(new OverclockingLogic((recipe1, recipeEUt, maxVoltage, duration, amountOC) -> {
                var pair = OverclockingLogic.NON_PERFECT_OVERCLOCK.getLogic().runOverclockingLogic(recipe1, recipeEUt, maxVoltage, duration, amountOC);
                if (coilMachine.getCoilTier() == 0) {
                    pair.second(pair.secondInt() * 5 / 4);
                } else {
                    pair.second(pair.secondInt() * 2 / (coilMachine.getCoilTier() + 1));
                }
                pair.second(Math.max(1, pair.secondInt()));
                return pair;
            }), recipe, coilMachine.getMaxVoltage());
        }
        return null;
    }

}
