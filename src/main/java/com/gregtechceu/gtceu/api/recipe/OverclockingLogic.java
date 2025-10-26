package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.google.common.math.IntMath;
import org.jetbrains.annotations.NotNull;

import java.math.RoundingMode;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents a function that, given an initial set of {@link OCParams} and a {@code maxVoltage},
 * will produce an {@link OCResult}
 */
@ParametersAreNonnullByDefault
@FunctionalInterface
public interface OverclockingLogic {

    OCResult runOverclockingLogic(@NotNull OCParams ocParams, long maxVoltage);

    double STD_VOLTAGE_FACTOR = 4.0;
    double PERFECT_HALF_VOLTAGE_FACTOR = 2.0;

    double STD_DURATION_FACTOR = 0.5;
    double STD_DURATION_FACTOR_INV = 2.0;

    double PERFECT_DURATION_FACTOR = 0.25;
    double PERFECT_DURATION_FACTOR_INV = 4.0;

    double PERFECT_HALF_DURATION_FACTOR = 0.5;
    double PERFECT_HALF_DURATION_FACTOR_INV = 2.0;

    int COIL_EUT_DISCOUNT_TEMPERATURE = 900;

    OverclockingLogic PERFECT_OVERCLOCK = create(PERFECT_DURATION_FACTOR, STD_VOLTAGE_FACTOR, false);
    OverclockingLogic NON_PERFECT_OVERCLOCK = create(STD_DURATION_FACTOR, STD_VOLTAGE_FACTOR, false);

    OverclockingLogic PERFECT_OVERCLOCK_SUBTICK = create(PERFECT_DURATION_FACTOR, STD_VOLTAGE_FACTOR, true);
    OverclockingLogic NON_PERFECT_OVERCLOCK_SUBTICK = create(STD_DURATION_FACTOR, STD_VOLTAGE_FACTOR, true);

    /**
     * Create a standard OverclockingLogic using either {@link #standardOC} or {@link #subTickParallelOC}
     * 
     * @param durationFactor the duration factor to use when overclocking
     * @param voltageFactor  the voltage factor to use when overclocking
     * @param subtick        whether the OverclockingLogic should apply subtick parallels or not
     * @return A new OverclockingLogic with the given parameters
     */
    static OverclockingLogic create(double durationFactor, double voltageFactor, boolean subtick) {
        if (subtick) return (params, maxV) -> subTickParallelOC(params, maxV, durationFactor, voltageFactor);
        else return (params, maxV) -> standardOC(params, maxV, durationFactor, voltageFactor);
    }

    /**
     * Determines overclocking parameters from the given arguments, runs the overclock, and returns a ModifierFunction
     * 
     * @param machine        machine
     * @param recipe         recipe
     * @param maxVoltage     max overclock voltage
     * @param shouldParallel whether the OC Logic should parallel or not
     * @return A {@link ModifierFunction} describing how the OC application should modify the recipe
     */
    default @NotNull ModifierFunction getModifier(MetaMachine machine, GTRecipe recipe, long maxVoltage,
                                                  boolean shouldParallel) {
        long EUt = RecipeHelper.getRealEUt(recipe).getTotalEU();
        if (EUt == 0) return ModifierFunction.IDENTITY;

        int recipeTier = GTUtil.getTierByVoltage(EUt);
        int maximumTier = GTUtil.getOCTierByVoltage(maxVoltage);
        int OCs = maximumTier - recipeTier;
        if (recipeTier == GTValues.ULV) OCs--;
        if (OCs == 0) return ModifierFunction.IDENTITY;

        int maxParallels;
        if (!shouldParallel || this == PERFECT_OVERCLOCK || this == NON_PERFECT_OVERCLOCK) { // don't parallel
            maxParallels = 1;
        } else {
            // lg = floor(log_4(duration)), which is how many OCs it takes to get duration < 4 with perfect duration
            // factor
            // If OCs <= lg, duration probably won't go below 4
            // If OCs > lg, then we could have 4^(OCs - lg) parallels
            // Note that 4^x = (2^2)^x = 2^(2x) = 1 << 2x
            int lg = IntMath.log2(recipe.duration, RoundingMode.FLOOR) / 2;
            if (lg > OCs) {
                maxParallels = 16;
            } else {
                int p = GTMath.saturatedCast((1L << (2 * (OCs - lg))) + 1);
                maxParallels = ParallelLogic.getParallelAmount(machine, recipe, p);
            }
        }

        OCParams params = new OCParams(EUt, recipe.duration, OCs, maxParallels);
        OCResult result = runOverclockingLogic(params, maxVoltage);
        return result.toModifier();
    }

    default @NotNull ModifierFunction getModifier(MetaMachine machine, GTRecipe recipe, long maxVoltage) {
        return getModifier(machine, recipe, maxVoltage, true);
    }

    /**
     * Standard overclocking algorithm with no sub-tick behavior.
     * <p>
     * While there are overclocks remaining:
     * <ol>
     * <li>Multiplies {@code EUt} by {@code voltageFactor}
     * <li>Multiplies {@code duration} by {@code durationFactor}
     * <li>Limit {@code duration} to {@code 1} tick, and stop overclocking early if needed
     * </ol>
     * 
     * @param params         the overclocking parameters
     * @param maxVoltage     the maximum voltage allowed to be overclocked to
     * @param durationFactor the factor to multiply duration by
     * @param voltageFactor  the factor to multiply voltage by
     * @return the result of the overclock
     */
    static OCResult standardOC(OCParams params, long maxVoltage, double durationFactor, double voltageFactor) {
        double duration = params.duration;
        double eut = params.eut;
        int ocAmount = params.ocAmount;
        int ocLevel = 0;

        while (ocAmount-- > 0) {
            // Check if EUt can be multiplied without going over the max
            double potentialEUt = eut * voltageFactor;
            if (potentialEUt > maxVoltage) break;

            // Check if duration can be multiplied without going below 1
            double potentialDuration = duration * durationFactor;
            if (potentialDuration < 1) break;
            duration = potentialDuration;

            // Only set EUt after checking duration - no need to OC if duration would be too low
            eut = potentialEUt;
            ocLevel++;
        }
        return new OCResult(Math.pow(voltageFactor, ocLevel), Math.pow(durationFactor, ocLevel), ocLevel, 1);
    }

    /**
     * Overclocking algorithm with sub-tick logic, which improves energy efficiency without parallelization.
     * <p>
     * While there are overclocks remaining:
     * <ol>
     * <li>Multiplies {@code EUt} by {@code voltageFactor}
     * <li>Multiplies {@code duration} by {@code durationFactor}
     * <li>Limit {@code duration} to {@code 1} tick
     * <li>Multiply {@code EUt} by {@code durationFactor} and maintain {@code duration} at {@code 1} tick for
     * overclocks that would have {@code duration < 1}
     * </ol>
     * 
     * @param params         the overclocking parameters
     * @param maxVoltage     the maximum voltage allowed to be overclocked to
     * @param durationFactor the factor to multiply duration by
     * @param voltageFactor  the factor to multiply voltage by
     * @return the result of the overclock
     */
    static OCResult subTickNonParallelOC(OCParams params, long maxVoltage, double durationFactor,
                                         double voltageFactor) {
        double duration = params.duration;
        double eut = params.eut;
        int ocAmount = params.ocAmount;

        int ocLevel = 0;
        double eutMultiplier = 1;
        double durationMultiplier = 1;

        while (ocAmount-- > 0) {
            double potentialEUt = eut * voltageFactor;
            if (potentialEUt > maxVoltage || potentialEUt < 1) break;
            eutMultiplier *= voltageFactor;

            double potentialDuration = duration * durationFactor;
            if (potentialDuration < 1) {
                potentialEUt = eut * durationFactor;
                if (potentialEUt > maxVoltage || potentialEUt < 1) break;
                eutMultiplier *= durationFactor;
            } else {
                duration = potentialDuration;
                durationMultiplier *= durationFactor;
            }

            eut = potentialEUt;
            ocLevel++;
        }

        return new OCResult(eutMultiplier, durationMultiplier, ocLevel, 1);
    }

    /**
     * Overclocking algorithm with sub-tick parallelization.
     * <p>
     * While there are overclocks remaining:
     * <ol>
     * <li>Multiplies {@code EUt} by {@code voltageFactor}
     * <li>Multiplies {@code duration} by {@code durationFactor}
     * <li>Limit {@code duration} to {@code 1} tick
     * <li>Parallelize {@code EUt} with {@code voltageFactor} and maintain {@code duration} at {@code 1} tick for
     * overclocks that would have {@code duration < 1}
     * <li>Parallel amount per overclock is {@code 1 / durationFactor}
     * </ol>
     * 
     * @param params         the overclocking parameters
     * @param maxVoltage     the maximum voltage allowed to be overclocked to
     * @param durationFactor the factor to multiply duration by
     * @param voltageFactor  the factor to multiply voltage by
     * @return the result of the overclock
     */
    static OCResult subTickParallelOC(OCParams params, long maxVoltage, double durationFactor, double voltageFactor) {
        double duration = params.duration;
        double eut = params.eut;
        int ocAmount = params.ocAmount;
        int maxParallels = params.maxParallels;

        double parallel = 1;
        boolean shouldParallel = false;
        int ocLevel = 0;
        double durationMultiplier = 1;

        while (ocAmount-- > 0) {
            // Check if EUt can be multiplied again without going over the max
            double potentialEUt = eut * voltageFactor;
            if (potentialEUt > maxVoltage) break;

            // If we're already doing parallels or our duration would go below 1, try parallels
            if (shouldParallel || duration * durationFactor < 1) {
                // Check if parallels can be multiplied without going over the maximum
                double potentialParallel = parallel / durationFactor;
                if (potentialParallel > maxParallels) break;
                parallel = potentialParallel;
                shouldParallel = true;
            } else {
                duration *= durationFactor;
                durationMultiplier *= durationFactor;
            }

            // Only set EUt after checking parallels - no need to OC if parallels would be too high
            eut = potentialEUt;
            ocLevel++;
        }

        return new OCResult(Math.pow(voltageFactor, ocLevel), durationMultiplier, ocLevel, (int) parallel);
    }

    /**
     * Heating Coil overclocking algorithm with sub-tick parallelization.
     * <p>
     * While there are overclocks remaining:
     * <ol>
     * <li>Multiplies {@code EUt} by {@link #STD_VOLTAGE_FACTOR}
     * <li>Multiplies {@code duration} by {@link #PERFECT_DURATION_FACTOR} if there are perfect OCs remaining,
     * otherwise multiplies by {@link #STD_DURATION_FACTOR}
     * <li>Limit {@code duration} to {@code 1} tick
     * <li>Parallelize {@code EUt} with {@link #STD_VOLTAGE_FACTOR} and maintain {@code duration} at {@code 1} tick for
     * overclocks that would have {@code duration < 1}
     * <li>Parallelization amount per overclock is {@link #PERFECT_DURATION_FACTOR_INV} if there are perfect OCs
     * remaining, otherwise uses {@link #STD_DURATION_FACTOR_INV}
     * <li>The maximum amount of perfect OCs is determined by {@link #getCoilDiscountAmount(int, int)}, divided
     * by 2.
     * </ol>
     * 
     * @param params      the overclocking parameters
     * @param maxVoltage  the maximum voltage allowed to be overclocked to
     * @param recipeTemp  the temperature required by the recipe
     * @param machineTemp the provided temperature
     */
    static OCResult heatingCoilOC(OCParams params, long maxVoltage, int recipeTemp, int machineTemp) {
        int perfectOCAmount = getCoilDiscountAmount(recipeTemp, machineTemp) / 2;
        double duration = params.duration;
        double eut = params.eut;
        int ocAmount = params.ocAmount;
        int maxParallels = params.maxParallels;

        double parallel = 1;
        boolean shouldParallel = false;
        int ocLevel = 0;
        double durationMultiplier = 1;

        while (ocAmount-- > 0) {
            // Do perfects first if possible
            boolean perfect = perfectOCAmount-- > 0;

            // Check if EUt can be multiplied again without going over the max
            double potentialEUt = eut * STD_VOLTAGE_FACTOR;
            if (potentialEUt > maxVoltage) break;

            // If we're already doing parallels or our duration would go below 1, try parallels
            double dFactor = (perfect ? PERFECT_DURATION_FACTOR : STD_DURATION_FACTOR);
            if (shouldParallel || duration * dFactor < 1) {
                // Check if parallels can be multiplied without going over the maximum
                double pFactor = perfect ? PERFECT_DURATION_FACTOR_INV : STD_DURATION_FACTOR_INV;
                double potentialParallel = parallel * pFactor;
                if (potentialParallel > maxParallels) break;
                parallel = potentialParallel;
                shouldParallel = true;
            } else {
                duration *= dFactor;
                durationMultiplier *= dFactor;
            }

            // Only set EUt after checking parallels - no need to OC if parallels would be too high
            eut = potentialEUt;
            ocLevel++;
        }

        return new OCResult(Math.pow(STD_VOLTAGE_FACTOR, ocLevel), durationMultiplier, ocLevel, (int) parallel);
    }

    /**
     * Finds the coil discount amount based on the recipe temp.
     * 
     * @param recipeTemp  the required temperature of the recipe
     * @param machineTemp the temperature provided by the machine
     * @return the amount of EU/t discounts to apply
     */
    private static int getCoilDiscountAmount(int recipeTemp, int machineTemp) {
        return Math.max(0, (machineTemp - recipeTemp) / COIL_EUT_DISCOUNT_TEMPERATURE);
    }

    /**
     * Calculates heating coil EU/t discount multiplier
     *
     * @param recipeTemp  the required temperature of the recipe
     * @param machineTemp the temperature provided by the machine
     * @return the EU/t discount multiplier
     */
    static double getCoilEUtDiscount(int recipeTemp, int machineTemp) {
        if (recipeTemp < COIL_EUT_DISCOUNT_TEMPERATURE) return 1;
        int amountEUtDiscount = getCoilDiscountAmount(recipeTemp, machineTemp);
        if (amountEUtDiscount < 1) return 1;
        return Math.min(1, Math.pow(0.95, amountEUtDiscount));
    }

    record OCParams(long eut, int duration, int ocAmount, int maxParallels) {}

    record OCResult(double eutMultiplier, double durationMultiplier, int ocLevel, int parallels) {

        public ModifierFunction toModifier() {
            return ModifierFunction.builder()
                    .modifyAllContents(ContentModifier.multiplier(parallels))
                    .eutMultiplier(eutMultiplier)
                    .durationMultiplier(durationMultiplier)
                    .addOCs(ocLevel)
                    .subtickParallels(parallels)
                    .build();
        }
    }
}
