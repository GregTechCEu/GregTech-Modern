package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
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

/**
 * @author KilaBash
 * @date 2023/7/9
 * @implNote GTRecipeModifiers
 */
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

    // Looks for parallel hatch and tries to do as many parallels as is set
    public static @NotNull ModifierFunction hatchParallel(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (machine instanceof IMultiController controller && controller.isFormed()) {
            int parallels = controller.getParts().stream()
                    .filter(IParallelHatch.class::isInstance)
                    .map(IParallelHatch.class::cast)
                    .findAny()
                    .map(hatch -> ParallelLogic.getParallelAmount(machine, recipe, hatch.getCurrentParallel()))
                    .orElse(1);

            return ModifierFunction.builder()
                    .modifyAllContents(ContentModifier.multiplier(parallels))
                    .eutMultiplier(parallels)
                    .parallels(parallels)
                    .build();
        }
        return ModifierFunction.IDENTITY;
    }

    // Non-perfect subtick OC + 10% EUt discount per coil
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

    // 5% EUt discount per 900K excess heat -> heating coil OC
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

    // Coil duration multipler -> non-perfect subtick OC
    public static @NotNull ModifierFunction pyrolyseOvenOverclock(@NotNull MetaMachine machine,
                                                                  @NotNull GTRecipe recipe) {
        if (!(machine instanceof CoilWorkableElectricMultiblockMachine coilMachine)) {
            return RecipeModifier.nullWrongType(CoilWorkableElectricMultiblockMachine.class, machine);
        }
        if (RecipeHelper.getRecipeEUtTier(recipe) > coilMachine.getTier()) return ModifierFunction.NULL;

        var oc = NON_PERFECT_OVERCLOCK_SUBTICK.getModifier(machine, recipe, coilMachine.getOverclockVoltage());

        int tier = coilMachine.getCoilTier();
        var durationModifier = ModifierFunction.builder();
        if (tier == 0) { // 75% speed with cupro coils
            durationModifier.durationMultiplier(4.0 / 3);
        } else {
            durationModifier.durationMultiplier(2.0 / (tier + 1));
        }
        return oc.compose(durationModifier.build());
    }

    // Set EUt and Duration to calculated values -> OC -> apply parallels
    public static @NotNull ModifierFunction multiSmelterParallel(@NotNull MetaMachine machine,
                                                                 @NotNull GTRecipe recipe) {
        if (!(machine instanceof CoilWorkableElectricMultiblockMachine coilMachine)) {
            return RecipeModifier.nullWrongType(CoilWorkableElectricMultiblockMachine.class, machine);
        }

        int maxParallel = 32 * coilMachine.getCoilType().getLevel();
        final int FURNACE_DURATION = 128;
        int parallels = ParallelLogic.getParallelAmount(machine, recipe, maxParallel);
        if (parallels == 0) return ModifierFunction.NULL;

        int dur = (int) Math.max(1, FURNACE_DURATION * 2 * parallels / Math.max(1.0, maxParallel));
        long eut = 4 * Math.max(1, (parallels / 8) / coilMachine.getCoilType().getEnergyDiscount());
        ModifierFunction baseModifier = r -> {
            var copy = r.copy();
            EURecipeCapability.putEUContent(copy.tickInputs, eut);
            copy.duration = dur;
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
