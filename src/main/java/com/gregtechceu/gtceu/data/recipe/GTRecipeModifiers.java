package com.gregtechceu.gtceu.data.recipe;

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
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.medicalcondition.GTMedicalConditions;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.LongIntPair;
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

    public static final RecipeModifier SUBTICK_PARALLEL = (machine, recipe) -> GTRecipeModifiers
            .subtickParallel(machine, recipe, false);

    public static final BiFunction<MedicalCondition, Integer, RecipeModifier> ENVIRONMENT_REQUIREMENT = Util
            .memoize((condition, maxAllowedStrength) -> (machine, recipe) -> {
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
                recipe = new RecipeHolder<>(recipe.id(), recipe.value().copy());
                int originalDuration = recipe.value().duration;
                recipe.value().duration *= (1 + (int) (strength * 5 / maxAllowedStrength));
                if (recipe.value().duration > 5 * originalDuration) {
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
        public RecipeHolder<GTRecipe> apply(MetaMachine machine, @NotNull RecipeHolder<GTRecipe> recipe) {
            if (machine instanceof IOverclockMachine overclockMachine) {
                if (RecipeHelper.getRecipeEUtTier(recipe.value()) / recipe.value().parallels >
                        overclockMachine.getMaxOverclockTier()) {
                    return null;
                }
                return RecipeHelper.applyOverclock(overclockingLogic, recipe, overclockMachine.getOverclockVoltage());
            }
            if (machine instanceof ITieredMachine tieredMachine &&
                    RecipeHelper.getRecipeEUtTier(recipe.value()) > tieredMachine.getTier()) {
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
    public static Pair<RecipeHolder<GTRecipe>, Integer> fastParallel(MetaMachine machine,
                                                                     @NotNull RecipeHolder<GTRecipe> recipe,
                                                                     int maxParallel,
                                                                     boolean modifyDuration) {
        if (machine instanceof IRecipeCapabilityHolder holder) {
            while (maxParallel > 0) {
                var copied = recipe.value().copy(ContentModifier.multiplier(maxParallel), modifyDuration);
                recipe = new RecipeHolder<>(recipe.id(), copied);
                if (GTRecipe.matchRecipe(recipe, holder).isSuccess() &&
                        GTRecipe.matchTickRecipe(recipe, holder).isSuccess()) {
                    return Pair.of(recipe, maxParallel);
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
    public static Pair<RecipeHolder<GTRecipe>, Integer> accurateParallel(MetaMachine machine,
                                                                         @NotNull RecipeHolder<GTRecipe> recipe,
                                                                         int maxParallel, boolean modifyDuration) {
        if (maxParallel == 1) {
            return Pair.of(recipe, 1);
        }
        return ParallelLogic.applyParallel(machine, recipe, maxParallel, modifyDuration);
    }

    public static Pair<RecipeHolder<GTRecipe>, Integer> hatchParallel(MetaMachine machine,
                                                                      @NotNull RecipeHolder<GTRecipe> recipe,
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

    public static RecipeHolder<GTRecipe> crackerOverclock(MetaMachine machine, @NotNull RecipeHolder<GTRecipe> recipe) {
        if (machine instanceof CoilWorkableElectricMultiblockMachine coilMachine) {
            if (RecipeHelper.getRecipeEUtTier(recipe.value()) > coilMachine.getTier()) {
                return null;
            }
            return RecipeHelper
                    .applyOverclock(new OverclockingLogic((recipe1, recipeEUt, maxVoltage, duration, amountOC) -> {
                        var pair = OverclockingLogic.NON_PERFECT_OVERCLOCK.getLogic().runOverclockingLogic(
                                recipe.value(),
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

    public static RecipeHolder<GTRecipe> ebfOverclock(MetaMachine machine, @NotNull RecipeHolder<GTRecipe> recipe) {
        if (machine instanceof CoilWorkableElectricMultiblockMachine coilMachine) {
            final var blastFurnaceTemperature = coilMachine.getCoilType().getCoilTemperature() +
                    100 * Math.max(0, coilMachine.getTier() - GTValues.MV);
            if (!recipe.value().data.contains("ebf_temp") ||
                    recipe.value().data.getInt("ebf_temp") > blastFurnaceTemperature) {
                return null;
            }
            if (RecipeHelper.getRecipeEUtTier(recipe.value()) > coilMachine.getTier()) {
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
                                    recipe.value().data.contains("ebf_temp") ? recipe.value().data.getInt("ebf_temp") :
                                            0)),
                    recipe, coilMachine.getOverclockVoltage());
        }
        return null;
    }

    public static RecipeHolder<GTRecipe> pyrolyseOvenOverclock(MetaMachine machine,
                                                               @NotNull RecipeHolder<GTRecipe> recipe) {
        if (machine instanceof CoilWorkableElectricMultiblockMachine coilMachine) {
            if (RecipeHelper.getRecipeEUtTier(recipe.value()) > coilMachine.getTier()) {
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

    public static RecipeHolder<GTRecipe> multiSmelterParallel(MetaMachine machine,
                                                              @NotNull RecipeHolder<GTRecipe> recipe) {
        if (machine instanceof CoilWorkableElectricMultiblockMachine coilMachine) {

            var maxParallel = 32 * coilMachine.getCoilType().getLevel();

            var result = GTRecipeModifiers.accurateParallel(machine, recipe, maxParallel, false);
            recipe = result.getFirst() == recipe ? new RecipeHolder<>(recipe.id(), result.getFirst().value().copy()) :
                    result.getFirst();

            int parallelValue = result.getSecond();
            recipe.value().duration = Math.max(1, 256 * parallelValue / maxParallel);
            long eut = 4 * (parallelValue / 8) / coilMachine.getCoilType().getEnergyDiscount();
            recipe.value().tickInputs.put(EURecipeCapability.CAP, List.of(new Content(eut,
                    ChanceLogic.getMaxChancedValue(), ChanceLogic.getMaxChancedValue(), 0, null, null)));
            return recipe;
        }
        return null;
    }

    public static RecipeHolder<GTRecipe> subtickParallel(MetaMachine machine, @NotNull RecipeHolder<GTRecipe> recipe,
                                                         boolean modifyDuration) {
        if (machine instanceof WorkableElectricMultiblockMachine electricMachine) {
            final Pair<RecipeHolder<GTRecipe>, Integer>[] result = new Pair[] { null };
            RecipeHelper.applyOverclock(
                    new OverclockingLogic((recipe1, recipeEUt, maxVoltage, duration, amountOC) -> {
                        var parallel = OverclockingLogic.standardOverclockingLogicWithSubTickParallelCount(
                                Math.abs(recipeEUt),
                                maxVoltage,
                                duration,
                                amountOC,
                                OverclockingLogic.STANDARD_OVERCLOCK_DURATION_DIVISOR,
                                OverclockingLogic.STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER);

                        result[0] = GTRecipeModifiers.accurateParallel(machine, recipe, parallel.getRight(),
                                modifyDuration);
                        return LongIntPair.of(parallel.getLeft(), parallel.getMiddle());
                    }), recipe, electricMachine.getOverclockVoltage());
            if (result[0] != null) {
                return result[0].getFirst();
            }
        }
        return recipe;
    }
}
