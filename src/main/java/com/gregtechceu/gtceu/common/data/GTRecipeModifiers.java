package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.logic.OCParams;
import com.gregtechceu.gtceu.api.recipe.logic.OCResult;
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

import static com.gregtechceu.gtceu.api.recipe.OverclockingLogic.applyCoilEUtDiscount;

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
    public static final RecipeModifier PARALLEL_HATCH = (machine, recipe, params, result) -> GTRecipeModifiers
            .hatchParallel(machine, recipe, false, params, result);

    public static final BiFunction<MedicalCondition, Integer, RecipeModifier> ENVIRONMENT_REQUIREMENT = Util
            .memoize((condition, maxAllowedStrength) -> (machine, recipe, params, result) -> {
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
                float strength = zone.strength();
                if (strength > maxAllowedStrength) {
                    return null;
                }
                recipe = recipe.copy();
                int originalDuration = recipe.duration;
                recipe.duration *= (1 + (int) (strength * 5 / maxAllowedStrength));
                if (recipe.duration > 5 * originalDuration) {
                    return null;
                }
                return recipe;
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
        public GTRecipe apply(MetaMachine machine, @NotNull GTRecipe recipe, @NotNull OCParams params,
                              @NotNull OCResult result) {
            if (machine instanceof IOverclockMachine overclockMachine) {
                if (RecipeHelper.getRecipeEUtTier(recipe) > overclockMachine.getMaxOverclockTier()) {
                    return null;
                }

                return RecipeHelper.applyOverclock(overclockingLogic, recipe,
                        overclockMachine.getOverclockVoltage(),
                        params, result);
            }
            if (machine instanceof ITieredMachine tieredMachine &&
                    result.getEut() > tieredMachine.getTier()) {
                return null;
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

    public static GTRecipe hatchParallel(MetaMachine machine, @NotNull GTRecipe recipe,
                                         boolean modifyDuration,
                                         @NotNull OCParams params, @NotNull OCResult result) {
        if (machine instanceof IMultiController controller && controller.isFormed()) {
            Optional<IParallelHatch> optional = controller.getParts().stream().filter(IParallelHatch.class::isInstance)
                    .map(IParallelHatch.class::cast).findAny();
            if (optional.isPresent()) {
                IParallelHatch hatch = optional.get();
                var recipeEU = RecipeHelper.getInputEUt(recipe);
                var parallelRecipe = ParallelLogic.applyParallel(machine, recipe, hatch.getCurrentParallel(),
                        modifyDuration);
                if (parallelRecipe.getSecond() == 0)
                    return null;
                result.init(recipeEU, recipe.duration, parallelRecipe.getSecond(), params.getOcAmount());
                return parallelRecipe.getFirst();
            }
        }
        return recipe;
    }

    public static GTRecipe crackerOverclock(MetaMachine machine, @NotNull GTRecipe recipe, @NotNull OCParams params,
                                            @NotNull OCResult result) {
        if (machine instanceof CoilWorkableElectricMultiblockMachine coilMachine) {
            if (RecipeHelper.getRecipeEUtTier(recipe) > coilMachine.getTier()) {
                return null;
            }
            var re = RecipeHelper.applyOverclock(
                    new OverclockingLogic((p, r, maxVoltage) -> {
                        OverclockingLogic.NON_PERFECT_OVERCLOCK.getLogic()
                                .runOverclockingLogic(params, result, maxVoltage);
                    }), recipe, coilMachine.getOverclockVoltage(), params, result);

            if (coilMachine.getCoilTier() > 0) {
                result.setEut(Math.max(1, (long) (result.getEut() * (1.0 - coilMachine.getCoilTier() * 0.1))));
            }
            return re;
        }
        return null;
    }

    public static GTRecipe ebfOverclock(MetaMachine machine, @NotNull GTRecipe recipe, @NotNull OCParams params,
                                        @NotNull OCResult result) {
        if (machine instanceof CoilWorkableElectricMultiblockMachine coilMachine) {
            final var blastFurnaceTemperature = coilMachine.getCoilType().getCoilTemperature() +
                    100 * Math.max(0, coilMachine.getTier() - GTValues.MV);
            if (!recipe.data.contains("ebf_temp") || recipe.data.getInt("ebf_temp") > blastFurnaceTemperature) {
                return null;
            }
            if (RecipeHelper.getRecipeEUtTier(recipe) > coilMachine.getTier()) {
                return null;
            }

            recipe.tickInputs.put(EURecipeCapability.CAP, List.of(new Content(
                    applyCoilEUtDiscount(RecipeHelper.getInputEUt(recipe),
                            blastFurnaceTemperature, recipe.data.getInt("ebf_temp")),
                    ChanceLogic.getMaxChancedValue(), ChanceLogic.getMaxChancedValue(),
                    0, null, null)));

            return RecipeHelper.applyOverclock(
                    new OverclockingLogic((p, r, maxVoltage) -> OverclockingLogic.heatingCoilOC(
                            params, result, maxVoltage,
                            blastFurnaceTemperature,
                            recipe.data.contains("ebf_temp") ? recipe.data.getInt("ebf_temp") : 0)),
                    recipe, coilMachine.getOverclockVoltage(), params, result);
        }
        return null;
    }

    public static GTRecipe pyrolyseOvenOverclock(MetaMachine machine, @NotNull GTRecipe recipe,
                                                 @NotNull OCParams params, @NotNull OCResult result) {
        if (machine instanceof CoilWorkableElectricMultiblockMachine coilMachine) {
            if (RecipeHelper.getRecipeEUtTier(recipe) > coilMachine.getTier()) {
                return null;
            }

            var re = RecipeHelper.applyOverclock(new OverclockingLogic((p, r, maxVoltage) -> {
                OverclockingLogic.NON_PERFECT_OVERCLOCK.getLogic()
                        .runOverclockingLogic(params, result, maxVoltage);
            }), recipe, coilMachine.getOverclockVoltage(), params, result);

            int tier = coilMachine.getCoilTier();

            if (coilMachine.getCoilTier() == 0) {
                // 75% speed with cupro coils
                result.setDuration(Math.max(1, (int) (result.getDuration() * 4.0 / 3)));
            } else {
                result.setDuration(Math.max(1, (int) (result.getDuration() * 2.0 / (tier + 1))));
            }
            return re;
        }
        return null;
    }

    public static GTRecipe multiSmelterParallel(MetaMachine machine, @NotNull GTRecipe recipe, @NotNull OCParams params,
                                                @NotNull OCResult result) {
        if (machine instanceof CoilWorkableElectricMultiblockMachine coilMachine) {

            var maxParallel = 32 * coilMachine.getCoilType().getLevel();
            final int FURNACE_DURATION = 128;
            var parallel = GTRecipeModifiers.accurateParallel(machine, recipe, maxParallel, false);

            if (parallel.getSecond() == 0) return null;

            int parallelValue = parallel.getSecond();
            long eut = 4 * Math.max(1, (parallelValue / 8) / coilMachine.getCoilType().getEnergyDiscount());

            recipe.duration = (int) Math.max(1, FURNACE_DURATION * 2 * parallelValue / Math.max(1.0, maxParallel));
            recipe.tickInputs.put(EURecipeCapability.CAP, List.of(new Content(eut,
                    ChanceLogic.getMaxChancedValue(), ChanceLogic.getMaxChancedValue(),
                    0, null, null)));

            RecipeHelper.applyOverclock(new OverclockingLogic((p, r, maxVoltage) -> {
                OverclockingLogic.NON_PERFECT_OVERCLOCK.getLogic()
                        .runOverclockingLogic(params, result, maxVoltage);
            }), recipe, coilMachine.getOverclockVoltage(), params, result);
            recipe = recipe.copy(ContentModifier.multiplier(parallelValue), false);

            return recipe;
        }
        return null;
    }
}
