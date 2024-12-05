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
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
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

import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.recipe.OverclockingLogic.*;

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
    public static final RecipeModifier PARALLEL_HATCH = GTRecipeModifiers::hatchParallel;

    public static final BiFunction<MedicalCondition, Integer, RecipeModifier> ENVIRONMENT_REQUIREMENT = Util
            .memoize((condition, maxAllowedStrength) -> (machine, recipe) -> {
                if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) return ModifierFunction.IDENTITY;
                Level level = machine.getLevel();
                if (!(level instanceof ServerLevel serverLevel)) {
                    return null;
                }
                EnvironmentalHazardSavedData data = EnvironmentalHazardSavedData.getOrCreate(serverLevel);
                BlockPos machinePos = machine.getPos();
                var zone = data.getZoneByContainedPosAndCondition(machinePos, condition);
                if (zone == null) {
                    return ModifierFunction.IDENTITY;
                }

                float strength = zone.strength();
                if (strength > maxAllowedStrength) {
                    return null;
                }

                int multiplier = (1 + (int) (strength * 5 / maxAllowedStrength));
                if (multiplier > 5) return null;

                return ModifierFunction.builder()
                        .durationModifier(ContentModifier.multiplier(multiplier))
                        .build();
            });

    public static final RecipeModifier DEFAULT_ENVIRONMENT_REQUIREMENT = ENVIRONMENT_REQUIREMENT
            .apply(GTMedicalConditions.CARBON_MONOXIDE_POISONING, 1000);

    @MethodsReturnNonnullByDefault
    @ParametersAreNonnullByDefault
    public static class ElectricOverclockModifier implements RecipeModifier {

        private final OverclockingLogic overclockingLogic;

        public ElectricOverclockModifier(OverclockingLogic overclockingLogic) {
            this.overclockingLogic = overclockingLogic;
        }

        @Nullable
        @Override
        public ModifierFunction getModifier(MetaMachine machine, @NotNull GTRecipe recipe) {
            if (!(machine instanceof IOverclockMachine overclockMachine)) return ModifierFunction.IDENTITY;
            if (RecipeHelper.getRecipeEUtTier(recipe) > overclockMachine.getMaxOverclockTier()) {
                return null;
            }
            return overclockingLogic.applyOverclock(recipe, overclockMachine.getOverclockVoltage());
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
    public static Pair<GTRecipe, Integer> fastParallel(MetaMachine machine, @NotNull GTRecipe recipe, int maxParallel) {
        if (machine instanceof IRecipeCapabilityHolder holder) {
            while (maxParallel > 0) {
                var copied = recipe.copy(ContentModifier.multiplier(maxParallel), false);
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
    public static Pair<ModifierFunction, Integer> accurateParallel(MetaMachine machine, @NotNull GTRecipe recipe,
                                                                   int maxParallel) {
        return ParallelLogic.applyParallel(machine, recipe, maxParallel);
    }

    public static ModifierFunction hatchParallel(MetaMachine machine, @NotNull GTRecipe recipe) {
        if (machine instanceof IMultiController controller && controller.isFormed()) {
            return controller.getParts().stream()
                    .filter(IParallelHatch.class::isInstance)
                    .map(IParallelHatch.class::cast)
                    .findAny()
                    .map(hatch -> ParallelLogic.applyParallel(machine, recipe, hatch.getCurrentParallel()))
                    .map(Pair::getFirst)
                    .orElse(ModifierFunction.IDENTITY);
        }
        return ModifierFunction.IDENTITY;
    }

    public static ModifierFunction crackerOverclock(MetaMachine machine, @NotNull GTRecipe recipe) {
        if (machine instanceof CoilWorkableElectricMultiblockMachine coilMachine) {
            if (RecipeHelper.getRecipeEUtTier(recipe) > coilMachine.getTier()) {
                return null;
            }

            var oc = OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK.applyOverclock(recipe,
                    coilMachine.getOverclockVoltage());
            if (coilMachine.getCoilTier() > 0) {
                var coilModifier = ModifierFunction.builder()
                        .eutModifier(ContentModifier.multiplier(1.0 - coilMachine.getCoilTier() * 0.1))
                        .build();
                oc = oc.compose(coilModifier);
            }
            return oc;
            ////
            //// new OverclockingLogic((p, r, maxVoltage) -> {
            //// OverclockingLogic.NON_PERFECT_OVERCLOCK
            //// .runOverclockingLogic(params, result, maxVoltage);
            //// }), recipe, coilMachine.getOverclockVoltage(), params, result);
            //
            // if (coilMachine.getCoilTier() > 0) {
            // result.setEut(Math.max(1, (long) (result.getEut() * (1.0 - coilMachine.getCoilTier() * 0.1))));
            // }
            // return re;
        }
        return null;
    }

    public static ModifierFunction ebfOverclock(MetaMachine machine, @NotNull GTRecipe recipe) {
        if (machine instanceof CoilWorkableElectricMultiblockMachine coilMachine) {
            int blastFurnaceTemperature = coilMachine.getCoilType().getCoilTemperature() +
                    100 * Math.max(0, coilMachine.getTier() - GTValues.MV);
            int recipeTemp = recipe.data.getInt("ebf_temp");
            if (!recipe.data.contains("ebf_temp") || recipeTemp > blastFurnaceTemperature) {
                return null;
            }
            if (RecipeHelper.getRecipeEUtTier(recipe) > coilMachine.getTier()) {
                return null;
            }

            var discount = ModifierFunction.builder()
                    .eutModifier(ContentModifier.multiplier(getCoilEUtDiscount(blastFurnaceTemperature, recipeTemp)))
                    .build();

            OverclockingLogic logic = (p, v) -> OverclockingLogic.heatingCoilOC(p, v, blastFurnaceTemperature,
                    recipeTemp);
            var oc = logic.applyOverclock(recipe, coilMachine.getOverclockVoltage());

            return oc.compose(discount);
        }
        return null;
    }

    public static ModifierFunction pyrolyseOvenOverclock(MetaMachine machine, @NotNull GTRecipe recipe) {
        if (machine instanceof CoilWorkableElectricMultiblockMachine coilMachine) {
            if (RecipeHelper.getRecipeEUtTier(recipe) > coilMachine.getTier()) {
                return null;
            }
            var oc = NON_PERFECT_OVERCLOCK_SUBTICK.applyOverclock(recipe, coilMachine.getOverclockVoltage());

            var builder = ModifierFunction.builder();
            int tier = coilMachine.getCoilTier();
            if (tier == 0) {
                // 75% speed with cupro coils
                builder.durationModifier(ContentModifier.multiplier(4.0 / 3));
            } else {
                builder.durationModifier(ContentModifier.multiplier(2.0 / (tier + 1)));
            }
            return oc.compose(builder.build());
        }
        return null;
    }

    public static ModifierFunction multiSmelterParallel(MetaMachine machine, @NotNull GTRecipe recipe) {
        if (machine instanceof CoilWorkableElectricMultiblockMachine coilMachine) {
            int maxParallel = 32 * coilMachine.getCoilType().getLevel();
            final int FURNACE_DURATION = 128;
            var parallel = ParallelLogic.applyParallel(machine, recipe, maxParallel);
            if (parallel.getSecond() == 0) return null;

            int parallelValue = parallel.getSecond();

            ModifierFunction parallelModifier = ModifierFunction.builder()
                    .inputModifier(ContentModifier.multiplier(parallelValue))
                    .outputModifier(ContentModifier.multiplier(parallelValue))
                    .multiplyParallels(parallelValue)
                    .build();

            long eut = 4 * Math.max(1, (parallelValue / 8) / coilMachine.getCoilType().getEnergyDiscount());

            var copy = recipe.copy();
            copy.duration = (int) Math.max(1, FURNACE_DURATION * 2 * parallelValue / Math.max(1.0, maxParallel));
            copy.tickInputs.put(EURecipeCapability.CAP, EURecipeCapability.makeEUContent(eut));

            var modifier = NON_PERFECT_OVERCLOCK.applyOverclock(copy, coilMachine.getOverclockVoltage());

            return modifier.compose(parallelModifier);
        }
        return null;
    }
}
