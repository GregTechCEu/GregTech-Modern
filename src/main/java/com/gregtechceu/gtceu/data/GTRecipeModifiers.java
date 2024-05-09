package com.gregtechceu.gtceu.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import lombok.val;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.util.Tuple;

import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
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
    public static final Function<OverclockingLogic, RecipeModifier> ELECTRIC_OVERCLOCK = Util.memoize(ElectricOverclockModifier::new);
    public static final RecipeModifier PARALLEL_HATCH = (machine, recipe) -> GTRecipeModifiers.hatchParallel(machine, recipe, false).getA();

    @MethodsReturnNonnullByDefault
    @ParametersAreNonnullByDefault
    public static class ElectricOverclockModifier implements RecipeModifier {
        private final OverclockingLogic overclockingLogic;


        public ElectricOverclockModifier(OverclockingLogic overclockingLogic) {
            this.overclockingLogic = overclockingLogic;
        }
        @Override
        public GTRecipe apply(MetaMachine machine, GTRecipe recipe) {
            if (machine instanceof ITieredMachine tieredMachine && RecipeHelper.getRecipeEUtTier(recipe) > tieredMachine.getTier()) {
                return null;
            }
            if (machine instanceof IOverclockMachine overclockMachine) {
                return RecipeHelper.applyOverclock(overclockingLogic, recipe, overclockMachine.getOverclockVoltage());
            }
            return recipe;
        }
    }

    /**
     * Fast parallel, the parallel amount is always the 2 times the divisor of maxParallel。
     * @param machine recipe holder
     * @param recipe current recipe
     * @param maxParallel max parallel limited
     * @param modifyDuration should multiply the duration
     * @return modified recipe and parallel amount
     */
    public static Tuple<GTRecipe, Integer> fastParallel(MetaMachine machine, @NotNull GTRecipe recipe, int maxParallel, boolean modifyDuration) {
        if (machine instanceof IRecipeCapabilityHolder holder) {
            while (maxParallel > 0) {
                var copied = recipe.copy(ContentModifier.multiplier(maxParallel), modifyDuration);
                if (copied.matchRecipe(holder).isSuccess() && copied.matchTickRecipe(holder).isSuccess()) {
                    return new Tuple<>(copied, maxParallel);
                }
                maxParallel /= 2;
            }
        }
        return new Tuple<>(recipe, 1);
    };

    /**
     * Accurate parallel, always look for the maximum parallel value within maxParallel.
     * @param machine recipe holder
     * @param recipe current recipe
     * @param maxParallel max parallel limited
     * @param modifyDuration should multiply the duration
     * @return modified recipe and parallel amount
     */
    public static Tuple<GTRecipe, Integer> accurateParallel(MetaMachine machine, @NotNull GTRecipe recipe, int maxParallel, boolean modifyDuration) {
        if (maxParallel == 1) {
            return new Tuple<>(recipe, 1);
        }
//        if(!(machine instanceof ITieredMachine))return new Tuple<>(recipe, 1);
        if (machine instanceof IRecipeCapabilityHolder holder) {
            var parallel = tryParallel(holder, recipe, 1, maxParallel, modifyDuration);
            return parallel == null ? new Tuple<>(recipe, 1) : parallel;
        }
        return null;
    }

    private static Tuple<GTRecipe, Integer> tryParallel(IRecipeCapabilityHolder holder, GTRecipe original, int min, int max, boolean modifyDuration) {
        if (min > max) return null;

        int mid = (min + max) / 2;
        GTRecipe copied = original.copy(ContentModifier.multiplier(mid), modifyDuration);
        if (!copied.matchRecipe(holder).isSuccess() || !copied.matchTickRecipe(holder).isSuccess() ||
            !(holder instanceof ITieredMachine && RecipeHelper.getRecipeEUtTier(copied) <= ((ITieredMachine)holder).getTier())) {
            // tried too many
            return tryParallel(holder, original, min, mid - 1, modifyDuration);
        } else {
            // at max parallels
            if (mid == max) {
                return new Tuple<>(copied, mid);
            }
            // matches, but try to do more
            var tryMore = tryParallel(holder, original, mid + 1, max, modifyDuration);
            return tryMore != null ? tryMore : new Tuple<>(copied, mid);
        }
    }

    public static Tuple<GTRecipe, Integer> hatchParallel(MetaMachine machine, @NotNull GTRecipe recipe, boolean modifyDuration) {
        if (machine instanceof IMultiController controller && controller.isFormed()) {
            Optional<IParallelHatch> optional = controller.getParts().stream().filter(IParallelHatch.class::isInstance).map(IParallelHatch.class::cast).findAny();
            if (optional.isPresent()) {
                IParallelHatch hatch = optional.get();
                return accurateParallel(machine, recipe, hatch.getCurrentParallel(), modifyDuration);
            }
        }
        return new Tuple<>(recipe, 1);
    }

    public static GTRecipe crackerOverclock(MetaMachine machine, @NotNull GTRecipe recipe) {
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
            }), recipe, coilMachine.getOverclockVoltage());
        }
        return null;
    }

    public static GTRecipe ebfOverclock(MetaMachine machine, @NotNull GTRecipe recipe) {
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
                    recipe.data.contains("ebf_temp") ? recipe.data.getInt("ebf_temp") : 0
            )), recipe, coilMachine.getOverclockVoltage());
        }
        return null;
    }

    public static GTRecipe pyrolyseOvenOverclock(MetaMachine machine, @NotNull GTRecipe recipe) {
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
            }), recipe, coilMachine.getOverclockVoltage());
        }
        return null;
    }

    public static GTRecipe multiSmelterOverclock(MetaMachine machine, @NotNull GTRecipe recipe) {
        if (machine instanceof CoilWorkableElectricMultiblockMachine coilMachine) {
            var energyCost = Math.max(1L, 16 / coilMachine.getCoilType().getEnergyDiscount());
            var maxParallel = 32 * coilMachine.getCoilType().getLevel();
            var parallelLimit = Math.min(maxParallel, (int) (coilMachine.getOverclockVoltage() / energyCost));

            var result = GTRecipeModifiers.accurateParallel(machine, recipe, parallelLimit, false);
            recipe = result.getA() == recipe ? result.getA().copy() : result.getA();

            int parallelValue = result.getB();
            recipe.duration = Math.max(1, 256 * parallelValue / maxParallel);
            long eut = parallelValue * energyCost;
            recipe.tickInputs.put(EURecipeCapability.CAP, List.of(new Content(eut, 1.0f, 0.0f, null, null)));
            return recipe;
        }
        return null;
    }

}
