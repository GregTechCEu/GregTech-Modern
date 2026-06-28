package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentListMap;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.config.ConfigHolder;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.gregtechceu.gtceu.api.recipe.OverclockingLogic.*;
import static com.gregtechceu.gtceu.api.recipe.RecipeHelper.doTrim;

public class GTRecipeModifiers {

    /**
     * Given an {@link OverclockingLogic}, creates a {@link RecipeModifier} designed for an {@link IOverclockMachine}
     */
    public static final Function<OverclockingLogic, RecipeModifier> ELECTRIC_OVERCLOCK = Util
            .memoize(logic -> (machine, group, recipe) -> {
                if (!(machine instanceof IOverclockMachine overclockMachine)) return null;
                if (RecipeHelper.getRecipeEUtTier(recipe) > overclockMachine.getMaxOverclockTier()) {
                    return Component.translatable("gtceu.recipe_modifier.insufficient_voltage");
                }
                return logic.getModifier(machine, group, recipe, overclockMachine.getOverclockVoltage());
            });

    // Shortcuts for common OC logics
    public static final RecipeModifier OC_PERFECT = ELECTRIC_OVERCLOCK.apply(PERFECT_OVERCLOCK);
    public static final RecipeModifier OC_NON_PERFECT = ELECTRIC_OVERCLOCK.apply(NON_PERFECT_OVERCLOCK);
    public static final RecipeModifier OC_PERFECT_SUBTICK = ELECTRIC_OVERCLOCK.apply(PERFECT_OVERCLOCK_SUBTICK);

    public static final BiFunction<MedicalCondition, Integer, RecipeModifier> ENVIRONMENT_REQUIREMENT = Util
            .memoize((condition, maxAllowedStrength) -> (machine, group, recipe) -> {
                if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) return null;
                if (!(machine.getLevel() instanceof ServerLevel serverLevel)) return RecipeModifier.DEFAULT_FAILURE;

                EnvironmentalHazardSavedData data = EnvironmentalHazardSavedData.getOrCreate(serverLevel);
                BlockPos machinePos = machine.getPos();
                var zone = data.getZoneByContainedPosAndCondition(machinePos, condition);
                if (zone == null) return null;

                float strength = zone.strength();
                if (strength > maxAllowedStrength) return RecipeModifier.DEFAULT_FAILURE;

                int multiplier = (1 + (int) (strength * 5 / maxAllowedStrength));
                if (multiplier > 5) return RecipeModifier.DEFAULT_FAILURE;

                recipe.multiplyDuration(multiplier);
                return null;
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
     * @return the failure reason, or {@code null} on success
     */
    public static @Nullable Component hatchParallel(@NotNull MetaMachine machine, RecipeHandlerGroup group, @NotNull GTRecipe recipe) {
        if (machine instanceof IMultiController controller && controller.isFormed()) {
            int parallels = controller.getParallelHatch()
                    .map(hatch -> ParallelLogic.getParallelAmount(group, recipe, hatch.getCurrentParallel()))
                    .orElse(1);

            if (parallels <= 1) return null;
            recipe.multiplyAllContents(parallels);
            recipe.parallels *= parallels;
        }
        return null;
    }

    public static @Nullable Component batchMode(@NotNull MetaMachine machine, RecipeHandlerGroup group, @NotNull GTRecipe recipe) {
        if (machine instanceof IMultiController controller && controller.isFormed() && controller.isBatchEnabled()) {
            if (recipe.duration < ConfigHolder.INSTANCE.machines.batchDuration) {
                int parallel = ConfigHolder.INSTANCE.machines.batchDuration / recipe.duration;
                parallel = ParallelLogic.getParallelAmount(group, recipe, parallel, false);

                if (parallel <= 1) return null;

                recipe.multiplyInputs(parallel);
                recipe.multiplyOutputs(parallel);
                recipe.multiplyDuration(parallel);
                recipe.batchParallels *= parallel;
            }
        }
        return null;
    }

    /**
     * Recipe Modifier for <b>Cracker Multiblocks</b> - can be used as a valid {@link RecipeModifier}
     * <p>
     * Recipe is OC'd via {@link OverclockingLogic#NON_PERFECT_OVERCLOCK}.
     * Then, EUt is multiplied by {@code 1 - (0.1 × coilTier)}
     * </p>
     *
     * @param machine a {@link CoilWorkableElectricMultiblockMachine} used for Cracking
     * @param recipe  recipe
     * @return the failure reason, or {@code null} on success
     */
    public static @Nullable Component crackerOverclock(@NotNull MetaMachine machine, RecipeHandlerGroup group,
                                                       @NotNull GTRecipe recipe) {
        if (!(machine instanceof CoilWorkableElectricMultiblockMachine coilMachine)) {
            return RecipeModifier.nullWrongType(CoilWorkableElectricMultiblockMachine.class, machine);
        }
        if (RecipeHelper.getRecipeEUtTier(recipe) > coilMachine.getTier()) {
            return Component.translatable("gtceu.recipe_modifier.insufficient_voltage");
        }


        var failReason = OverclockingLogic.NON_PERFECT_OVERCLOCK.getModifier(machine, group, recipe,
                coilMachine.getOverclockVoltage());
        if (failReason != null) return failReason;
        if (coilMachine.getCoilTier() > 0) {
            recipe.multiplyEUt(1.0 - coilMachine.getCoilTier() * 0.1);
        }
        return null;
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
     * @return the failure reason, or {@code null} on success
     */
    public static @Nullable Component ebfOverclock(@NotNull MetaMachine machine, RecipeHandlerGroup group,
                                                   @NotNull GTRecipe recipe) {
        if (!(machine instanceof CoilWorkableElectricMultiblockMachine coilMachine)) {
            return RecipeModifier.nullWrongType(CoilWorkableElectricMultiblockMachine.class, machine);
        }

        int blastFurnaceTemperature = coilMachine.getCoilType().getCoilTemperature() +
                (100 * Math.max(0, coilMachine.getTier() - GTValues.MV));
        int recipeTemp = recipe.data.getInt("ebf_temp");
        if (!recipe.data.contains("ebf_temp") || recipeTemp > blastFurnaceTemperature) {
            return Component.translatable("gtceu.recipe_modifier.coil_temperature_too_low");
        }

        if (RecipeHelper.getRecipeEUtTier(recipe) > coilMachine.getTier()) {
            return Component.translatable("gtceu.recipe_modifier.insufficient_voltage");
        }

        recipe.multiplyEUt(getCoilEUtDiscount(recipeTemp, blastFurnaceTemperature));

        OverclockingLogic logic = (p, v) -> OverclockingLogic.heatingCoilOC(p, v, recipeTemp, blastFurnaceTemperature);
        return logic.getModifier(machine, group, recipe, coilMachine.getOverclockVoltage());
    }

    /**
     * Recipe Modifier for <b>Pyrolyse Oven Multiblocks</b> - can be used as a valid {@link RecipeModifier}
     * <p>
     * Recipe is OC'd via {@link OverclockingLogic#NON_PERFECT_OVERCLOCK}.<br>
     * Then, duration is multiplied by {@code 1.333×} for Cupronickel Coils
     * or {@code 2 / (tier + 1)} for higher tiercoils.
     * </p>
     *
     * @param machine a {@link CoilWorkableElectricMultiblockMachine} used for Pyrolysis
     * @param recipe  recipe
     * @return the failure reason, or {@code null} on success
     */
    public static @Nullable Component pyrolyseOvenOverclock(@NotNull MetaMachine machine,
                                                            RecipeHandlerGroup group,
                                                            @NotNull GTRecipe recipe) {
        if (!(machine instanceof CoilWorkableElectricMultiblockMachine coilMachine)) {
            return RecipeModifier.nullWrongType(CoilWorkableElectricMultiblockMachine.class, machine);
        }
        if (RecipeHelper.getRecipeEUtTier(recipe) > coilMachine.getTier()){
            return Component.translatable("gtceu.recipe_modifier.insufficient_voltage");
        }

        int tier = coilMachine.getCoilTier();
        double durationMultiplier = (tier == 0) ? (4.0 / 3.0) : (2.0 / (tier + 1)); // 75% speed with cupro coils

        var failReason = NON_PERFECT_OVERCLOCK.getModifier(machine, group, recipe, coilMachine.getOverclockVoltage());
        if (failReason != null) return failReason;
        recipe.multiplyDuration(durationMultiplier);
        return null;
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
     * @return the failure reason, or {@code null} on success
     */
    public static @Nullable Component multiSmelterParallel(@NotNull MetaMachine machine,
                                                           RecipeHandlerGroup group,
                                                           @NotNull GTRecipe recipe) {
        if (!(machine instanceof CoilWorkableElectricMultiblockMachine coilMachine)) {
            return RecipeModifier.nullWrongType(CoilWorkableElectricMultiblockMachine.class, machine);
        }

        int maxParallel = 32 * coilMachine.getCoilType().getLevel();
        int parallels = ParallelLogic.getParallelAmount(group, recipe, maxParallel);
        if (parallels <= 1) return null;

        int duration = (int) (128 * 2.0 * parallels / maxParallel);
        long eut = (long) (4L * maxParallel / (8.0 * coilMachine.getCoilType().getEnergyDiscount()));
        EURecipeCapability.putEUContent(recipe.tickInputs, new EnergyStack(Math.max(1, eut)));
        recipe.duration = Math.max(1, duration);

        var failReason = NON_PERFECT_OVERCLOCK.getModifier(machine, group, recipe, coilMachine.getOverclockVoltage());
        if (failReason != null) return failReason;
        recipe.multiplyAllContents(parallels);
        recipe.parallels *= parallels;
        return null;
    }

    public static RecipeModifier trimRecipeOutputs(Reference2IntMap<RecipeCapability<?>> trimLimits) {
        if (trimLimits.isEmpty() || trimLimits.values().intStream().allMatch(integer -> integer == -1)) {
            return RecipeModifier.NO_MODIFIER;
        }
        return (machine, group, recipe) -> {
            doTrim(recipe.outputs, trimLimits);
            doTrim(recipe.tickOutputs, trimLimits);
            return null;
        };
    }
}
