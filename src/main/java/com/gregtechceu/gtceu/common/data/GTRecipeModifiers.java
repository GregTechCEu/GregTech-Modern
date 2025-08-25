package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.gregtechceu.gtceu.api.recipe.OverclockingLogic.*;

public class GTRecipeModifiers {

    /**
     * Given an {@link OverclockingLogic}, creates a {@link RecipeModifier} designed for an {@link IOverclockMachine}
     */
    public static final Function<OverclockingLogic, RecipeModifier> ELECTRIC_OVERCLOCK = Util
            .memoize(logic -> (machine, recipe) -> {
                if (!(machine instanceof IOverclockMachine overclockMachine)) return ModifierFunction.IDENTITY;
                if (RecipeHelper.getRecipeEUtTier(recipe) > overclockMachine.getMaxOverclockTier()) {
                    return ModifierFunction.NULL;
                }
                return logic.getModifier(machine, recipe, overclockMachine.getOverclockVoltage());
            });

    // Shortcuts for common OC logics
    public static final RecipeModifier OC_PERFECT = ELECTRIC_OVERCLOCK.apply(PERFECT_OVERCLOCK);
    public static final RecipeModifier OC_NON_PERFECT = ELECTRIC_OVERCLOCK.apply(NON_PERFECT_OVERCLOCK);
    public static final RecipeModifier OC_PERFECT_SUBTICK = ELECTRIC_OVERCLOCK.apply(PERFECT_OVERCLOCK_SUBTICK);
    public static final RecipeModifier OC_NON_PERFECT_SUBTICK = ELECTRIC_OVERCLOCK.apply(NON_PERFECT_OVERCLOCK_SUBTICK);

    public static final BiFunction<MedicalCondition, Integer, RecipeModifier> ENVIRONMENT_REQUIREMENT = Util
            .memoize((condition, maxAllowedStrength) -> (machine, recipe) -> {
                if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) return ModifierFunction.IDENTITY;
                if (!(machine.getLevel() instanceof ServerLevel serverLevel)) return ModifierFunction.NULL;

                EnvironmentalHazardSavedData data = EnvironmentalHazardSavedData.getOrCreate(serverLevel);
                BlockPos machinePos = machine.getPos();
                var zone = data.getZoneByContainedPosAndCondition(machinePos, condition);
                if (zone == null) return ModifierFunction.IDENTITY;

                float strength = zone.strength();
                if (strength > maxAllowedStrength) return ModifierFunction.NULL;

                int multiplier = (1 + (int) (strength * 5 / maxAllowedStrength));
                if (multiplier > 5) return ModifierFunction.NULL;

                return ModifierFunction.builder()
                        .durationMultiplier(multiplier)
                        .build();
            });

    public static final RecipeModifier DEFAULT_ENVIRONMENT_REQUIREMENT = ENVIRONMENT_REQUIREMENT
            .apply(GTMedicalConditions.CARBON_MONOXIDE_POISONING, 1000);

    public static final RecipeModifier PARALLEL_HATCH = GTRecipeModifiers::hatchParallel;
    public static final RecipeModifier BATCH_MODE = GTRecipeModifiers::batchMode;

    /**
     * Recipe Modifier for <b>Parallel Multiblock Machines</b> - can be used as a valid {@link RecipeModifier}
     * <p>
     * Looks for the Parallel Hatch on a Multiblock and attempts to parallelize the recipe up to the set amount
     * </p>
     *
     * @param machine an {@link IMultiController} machine
     * @param recipe  recipe
     * @return A {@link ModifierFunction} for the given Parallel Multiblock
     */
    public static @NotNull ModifierFunction hatchParallel(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (machine instanceof IMultiController controller && controller.isFormed()) {
            int parallels = controller.getParallelHatch()
                    .map(hatch -> ParallelLogic.getParallelAmount(machine, recipe, hatch.getCurrentParallel()))
                    .orElse(1);

            if (parallels == 1) return ModifierFunction.IDENTITY;
            return ModifierFunction.builder()
                    .modifyAllContents(ContentModifier.multiplier(parallels))
                    .eutMultiplier(parallels)
                    .parallels(parallels)
                    .build();
        }
        return ModifierFunction.IDENTITY;
    }

    public static @NotNull ModifierFunction batchMode(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (machine instanceof IMultiController controller && controller.isFormed() && controller.isBatchEnabled()) {
            if (recipe.duration < ConfigHolder.INSTANCE.machines.batchDuration) {
                int parallel = ConfigHolder.INSTANCE.machines.batchDuration / recipe.duration;
                parallel = ParallelLogic.getParallelAmountWithoutEU(machine, recipe, parallel);

                if (parallel == 0) return ModifierFunction.NULL;
                if (parallel == 1) return ModifierFunction.IDENTITY;

                return ModifierFunction.builder()
                        .inputModifier(ContentModifier.multiplier(parallel))
                        .outputModifier(ContentModifier.multiplier(parallel))
                        .durationMultiplier(parallel)
                        .batchParallels(parallel)
                        .build();
            }
        }
        return ModifierFunction.IDENTITY;
    }

    /**
     * Recipe Modifier for <b>Cracker Multiblocks</b> - can be used as a valid {@link RecipeModifier}
     * <p>
     * Recipe is OC'd via {@link OverclockingLogic#NON_PERFECT_OVERCLOCK_SUBTICK}.
     * Then, EUt is multiplied by {@code 1 - (0.1 × coilTier)}
     * </p>
     *
     * @param machine a {@link CoilWorkableElectricMultiblockMachine} used for Cracking
     * @param recipe  recipe
     * @return A {@link ModifierFunction} for the given Cracker
     */
    public static @NotNull ModifierFunction crackerOverclock(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof CoilWorkableElectricMultiblockMachine coilMachine)) {
            return RecipeModifier.nullWrongType(CoilWorkableElectricMultiblockMachine.class, machine);
        }
        if (RecipeHelper.getRecipeEUtTier(recipe) > coilMachine.getTier()) return ModifierFunction.NULL;

        var oc = OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK.getModifier(machine, recipe,
                coilMachine.getOverclockVoltage());
        if (coilMachine.getCoilTier() > 0) {
            var coilModifier = ModifierFunction.builder()
                    .eutMultiplier(1.0 - coilMachine.getCoilTier() * 0.1)
                    .build();
            oc = oc.andThen(coilModifier);
        }
        return oc;
    }

    /**
     * Recipe Modifier for <b>Blast Furnace Multiblocks</b> - can be used as a valid {@link RecipeModifier}
     * <p>
     * Recipe is rejected if the required temperature is higher than the blast furnace's working temperature.
     * This working temperature is equal to {@code coilTemp + (100K × (voltageTier - MV))} for energy tiers over MV.
     * </p>
     * <p>
     * Recipe is OC'd via {@link OverclockingLogic#heatingCoilOC}.<br>
     * Then, EUt is multiplied by {@code 0.95×} for every {@code 900K} over the required temperature.
     * </p>
     *
     * @param machine a {@link CoilWorkableElectricMultiblockMachine} used for Blasting
     * @param recipe  recipe
     * @return A {@link ModifierFunction} for the given Blast Furnace
     */
    public static @NotNull ModifierFunction ebfOverclock(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof CoilWorkableElectricMultiblockMachine coilMachine)) {
            return RecipeModifier.nullWrongType(CoilWorkableElectricMultiblockMachine.class, machine);
        }

        int blastFurnaceTemperature = coilMachine.getCoilType().getCoilTemperature() +
                (100 * Math.max(0, coilMachine.getTier() - GTValues.MV));
        int recipeTemp = recipe.data.getInt("ebf_temp");
        if (!recipe.data.contains("ebf_temp") || recipeTemp > blastFurnaceTemperature) {
            return ModifierFunction.NULL;
        }

        if (RecipeHelper.getRecipeEUtTier(recipe) > coilMachine.getTier()) {
            return ModifierFunction.NULL;
        }

        var discount = ModifierFunction.builder()
                .eutMultiplier(getCoilEUtDiscount(recipeTemp, blastFurnaceTemperature))
                .build();

        OverclockingLogic logic = (p, v) -> OverclockingLogic.heatingCoilOC(p, v, recipeTemp, blastFurnaceTemperature);
        var oc = logic.getModifier(machine, recipe, coilMachine.getOverclockVoltage());

        return oc.compose(discount);
    }

    /**
     * Recipe Modifier for <b>Pyrolyse Oven Multiblocks</b> - can be used as a valid {@link RecipeModifier}
     * <p>
     * Recipe is OC'd via {@link OverclockingLogic#NON_PERFECT_OVERCLOCK_SUBTICK}.<br>
     * Then, duration is multiplied by {@code 1.333×} for Cupronickel Coils
     * or {@code 2 / (tier + 1)} for higher tiercoils.
     * </p>
     *
     * @param machine a {@link CoilWorkableElectricMultiblockMachine} used for Pyrolysis
     * @param recipe  recipe
     * @return A {@link ModifierFunction} for the given Pyrolyse Oven
     */
    public static @NotNull ModifierFunction pyrolyseOvenOverclock(@NotNull MetaMachine machine,
                                                                  @NotNull GTRecipe recipe) {
        if (!(machine instanceof CoilWorkableElectricMultiblockMachine coilMachine)) {
            return RecipeModifier.nullWrongType(CoilWorkableElectricMultiblockMachine.class, machine);
        }
        if (RecipeHelper.getRecipeEUtTier(recipe) > coilMachine.getTier()) return ModifierFunction.NULL;

        int tier = coilMachine.getCoilTier();
        double durationMultiplier = (tier == 0) ? (4.0 / 3.0) : (2.0 / (tier + 1)); // 75% speed with cupro coils
        var durationModifier = ModifierFunction.builder()
                .durationMultiplier(durationMultiplier)
                .build();

        var oc = NON_PERFECT_OVERCLOCK_SUBTICK.getModifier(machine, recipe, coilMachine.getOverclockVoltage());
        return oc.andThen(durationModifier);
    }

    /**
     * Recipe Modifier for <b>Multi Smelters</b> - can be used as a valid {@link RecipeModifier}
     * <p>
     * Modifies the recipe in the following order:
     * <ol>
     * <li>Calculates the maximum parallels as {@code 32 × coilLevel}</li>
     * <li>Finds the actual parallel amount that the smelter can do</li>
     * <li>Sets the recipe duration to {@code 128 × 2 × parallels / maxParallels}</li>
     * <li>Sets the recipe EUt to {@code (4 × maxParallels / (8 × coilDiscount))}</li>
     * <li>Applies {@link OverclockingLogic#NON_PERFECT_OVERCLOCK} to this modified recipe</li>
     * <li>Multiplies the recipe contents by the parallel amount</li>
     * </ol>
     * </p>
     *
     * @param machine a {@link CoilWorkableElectricMultiblockMachine} used for parallel smelting
     * @param recipe  recipe
     * @return A {@link ModifierFunction} for the given Multi Smelter
     */
    public static @NotNull ModifierFunction multiSmelterParallel(@NotNull MetaMachine machine,
                                                                 @NotNull GTRecipe recipe) {
        if (!(machine instanceof CoilWorkableElectricMultiblockMachine coilMachine)) {
            return RecipeModifier.nullWrongType(CoilWorkableElectricMultiblockMachine.class, machine);
        }

        int maxParallel = 32 * coilMachine.getCoilType().getLevel();
        int parallels = ParallelLogic.getParallelAmount(machine, recipe, maxParallel);
        if (parallels == 0) return ModifierFunction.NULL;

        int duration = (int) (128 * 2.0 * parallels / maxParallel);
        long eut = (long) (4 * maxParallel / (8.0 * coilMachine.getCoilType().getEnergyDiscount()));
        ModifierFunction baseModifier = r -> {
            var copy = r.copy();
            EURecipeCapability.putEUContent(copy.tickInputs, new EnergyStack(Math.max(1, eut)));
            copy.duration = Math.max(1, duration);
            return copy;
        };

        GTRecipe copy = baseModifier.apply(recipe);
        var ocModifier = NON_PERFECT_OVERCLOCK.getModifier(machine, copy, coilMachine.getOverclockVoltage());
        var parallelModifier = ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallels))
                .parallels(parallels)
                .build();

        return baseModifier.andThen(ocModifier).andThen(parallelModifier);
    }
}
