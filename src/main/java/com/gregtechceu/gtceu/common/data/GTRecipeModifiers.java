package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/7/9
 * @implNote GTRecipeModifiers
 */
public class GTRecipeModifiers {

    /**
     * Use it if machines are {@link IOverclockMachine}.
     */
    public static final Function<OverclockingLogic, RecipeModifier> ELECTRIC_OVERCLOCK = Util
            .memoize(ElectricOverclockModifier::new);
    public static final RecipeModifier PARALLEL_HATCH = (machine, recipe) -> GTRecipeModifiers
            .hatchParallel(machine, recipe, false).getFirst();
    public static final BiFunction<MedicalCondition, Integer, RecipeModifier> ENVIRONMENT_REQUIREMENT = Util
            .memoize((condition, maxAllowedAffectedBlocks) -> (machine, recipe) -> {
                if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) return recipe;
                Level level = machine.getLevel();
                if (!(level instanceof ServerLevel serverLevel)) {
                    return null;
                }
                EnvironmentalHazardSavedData data = EnvironmentalHazardSavedData.getOrCreate(serverLevel);
                BlockPos machinePos = machine.getPos();
                var zone = data.getZoneByContainedPosAndCondition(machinePos, condition);
                if (zone == null) {
                    return recipe;
                }
                int strength = zone.strength();
                if (strength < maxAllowedAffectedBlocks) {
                    return recipe;
                }
                recipe = recipe.copy();
                recipe.duration *= Math.max(1, maxAllowedAffectedBlocks / Math.max(strength, 1));
                return recipe;
            });
    public static final RecipeModifier DEFAULT_ENVIRONMENT_REQUIREMENT = ENVIRONMENT_REQUIREMENT
            .apply(GTMedicalConditions.CARBON_MONOXIDE_POISONING, 500);

    @MethodsReturnNonnullByDefault
    @ParametersAreNonnullByDefault
    public static class ElectricOverclockModifier implements RecipeModifier {

        private final OverclockingLogic overclockingLogic;

        public ElectricOverclockModifier(OverclockingLogic overclockingLogic) {
            this.overclockingLogic = overclockingLogic;
        }

        @Nullable
        @Override
        public GTRecipe apply(MetaMachine machine, @NotNull GTRecipe recipe) {
            if (machine instanceof IOverclockMachine overclockMachine) {
                return RecipeHelper.applyOverclock(overclockingLogic, recipe, overclockMachine.getOverclockVoltage());
            }
            return recipe;
        }
    }

    /**
     * Fast parallel, the parallel amount is always the 2 times the divisor of maxParallel。
     *
     * @param machine        recipe holder
     * @param recipe         current recipe
     * @param maxParallel    max parallel limited
     * @param modifyDuration should multiply the duration
     * @return modified recipe and parallel amount
     */
    public static Pair<GTRecipe, Integer> fastParallel(MetaMachine machine, @NotNull GTRecipe recipe, int maxParallel,
                                                       boolean modifyDuration) {
        if (machine instanceof IRecipeCapabilityHolder holder) {
            while (maxParallel > 0) {
                var copied = recipe.copy(ContentModifier.multiplier(maxParallel), modifyDuration);
                if (copied.matchRecipe(holder).isSuccess() && copied.matchTickRecipe(holder).isSuccess()) {
                    return Pair.of(copied, maxParallel);
                }
                maxParallel /= 2;
            }
        }
        return Pair.of(recipe, 1);
    }

    /**
     * Accurate parallel, always look for the maximum parallel value within maxParallel.
     *
     * @param machine        recipe holder
     * @param recipe         current recipe
     * @param maxParallel    max parallel limited
     * @param modifyDuration should multiply the duration
     * @return modified recipe and parallel amount
     */
    public static Pair<GTRecipe, Integer> accurateParallel(MetaMachine machine, @NotNull GTRecipe recipe,
                                                           int maxParallel, boolean modifyDuration) {
        if (maxParallel == 1) {
            return Pair.of(recipe, 1);
        }
        return ParallelLogic.applyParallel(machine, recipe, maxParallel, modifyDuration);
    }

    public static Pair<GTRecipe, Integer> hatchParallel(MetaMachine machine, @NotNull GTRecipe recipe,
                                                        boolean modifyDuration) {
        if (machine instanceof IMultiController controller && controller.isFormed()) {
            Optional<IParallelHatch> optional = controller.getParts().stream().filter(IParallelHatch.class::isInstance)
                    .map(IParallelHatch.class::cast).findAny();
            if (optional.isPresent()) {
                IParallelHatch hatch = optional.get();
                return ParallelLogic.applyParallel(machine, recipe, hatch.getCurrentParallel(), modifyDuration);
            }
        }
        return Pair.of(recipe, 1);
    }

    public static GTRecipe crackerOverclock(MetaMachine machine, @NotNull GTRecipe recipe) {
        if (machine instanceof CoilWorkableElectricMultiblockMachine coilMachine) {
            if (RecipeHelper.getRecipeEUtTier(recipe) > coilMachine.getTier()) {
                return null;
            }
            return RecipeHelper
                    .applyOverclock(new OverclockingLogic((recipe1, recipeEUt, maxVoltage, duration, amountOC) -> {
                        var pair = OverclockingLogic.NON_PERFECT_OVERCLOCK.getLogic().runOverclockingLogic(recipe,
                                recipeEUt, maxVoltage, duration, amountOC);
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
            final var blastFurnaceTemperature = coilMachine.getCoilType().getCoilTemperature() +
                    100 * Math.max(0, coilMachine.getTier() - GTValues.MV);
            if (!recipe.data.contains("ebf_temp") || recipe.data.getInt("ebf_temp") > blastFurnaceTemperature) {
                return null;
            }
            if (RecipeHelper.getRecipeEUtTier(recipe) > coilMachine.getTier()) {
                return null;
            }
            return RecipeHelper.applyOverclock(
                    new OverclockingLogic((recipe1, recipeEUt, maxVoltage, duration, amountOC) -> OverclockingLogic
                            .heatingCoilOverclockingLogic(
                                    Math.abs(recipeEUt),
                                    maxVoltage,
                                    duration,
                                    amountOC,
                                    blastFurnaceTemperature,
                                    recipe.data.contains("ebf_temp") ? recipe.data.getInt("ebf_temp") : 0)),
                    recipe, coilMachine.getOverclockVoltage());
        }
        return null;
    }

    public static GTRecipe pyrolyseOvenOverclock(MetaMachine machine, @NotNull GTRecipe recipe) {
        if (machine instanceof CoilWorkableElectricMultiblockMachine coilMachine) {
            if (RecipeHelper.getRecipeEUtTier(recipe) > coilMachine.getTier()) {
                return null;
            }
            return RecipeHelper
                    .applyOverclock(new OverclockingLogic((recipe1, recipeEUt, maxVoltage, duration, amountOC) -> {
                        var pair = OverclockingLogic.NON_PERFECT_OVERCLOCK.getLogic().runOverclockingLogic(recipe1,
                                recipeEUt, maxVoltage, duration, amountOC);
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

    public static GTRecipe multiSmelterParallel(MetaMachine machine, @NotNull GTRecipe recipe) {
        if (machine instanceof CoilWorkableElectricMultiblockMachine coilMachine) {

            var maxParallel = 32 * coilMachine.getCoilType().getLevel();

            var result = GTRecipeModifiers.accurateParallel(machine, recipe, maxParallel, false);
            recipe = result.getFirst() == recipe ? result.getFirst().copy() : result.getFirst();

            int parallelValue = result.getSecond();
            recipe.duration = Math.max(1, 256 * parallelValue / maxParallel);
            long eut = 4 * (parallelValue / 8) / coilMachine.getCoilType().getEnergyDiscount();
            recipe.tickInputs.put(EURecipeCapability.CAP, List.of(new Content(eut, 1.0f, 0.0f, null, null)));
            return recipe;
        }
        return null;
    }
}
