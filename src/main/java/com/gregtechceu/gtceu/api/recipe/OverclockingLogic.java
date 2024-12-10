package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.utils.GTUtil;

import org.jetbrains.annotations.NotNull;

/**
 * A class for holding all the various Overclocking logics
 */
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

    static OverclockingLogic create(double durationFactor, double voltageFactor, boolean subtick) {
        if (subtick) return (p, v) -> subTickParallelOC(p, v, durationFactor, voltageFactor);
        else return (p, v) -> standardOverclockingLogic(p, v, durationFactor, voltageFactor);
    }

    default ModifierFunction getModifierNoParallel(@NotNull MetaMachine machine, @NotNull GTRecipe recipe,
                                                   long maxOverclockVoltage) {
        long EUt = Math.abs(RecipeHelper.getRealEUt(recipe));
        if (EUt > 0) {
            return performOverclocking(EUt, recipe.duration, maxOverclockVoltage, 1).asModifierFunction();
        }
        return ModifierFunction.IDENTITY;
    }

    /**
     * Calculates the overclocked Recipe's final duration and EU/t
     *
     * @param recipe the recipe to run
     * @return a new recipe
     */
    default ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe,
                                         long maxOverclockVoltage) {
        long EUt = Math.abs(RecipeHelper.getRealEUt(recipe));
        if (EUt > 0) {
            int recipeTier = GTUtil.getTierByVoltage(EUt);
            int maximumTier = GTUtil.getTierByVoltage(maxOverclockVoltage);

            int maxParallels;
            if (this == PERFECT_OVERCLOCK || this == NON_PERFECT_OVERCLOCK) { // short-circuit for these common OCs
                maxParallels = 1;
            } else if ((Math.pow(PERFECT_DURATION_FACTOR, maximumTier - recipeTier) * recipe.duration) > 1) {
                maxParallels = Integer.MAX_VALUE; // if duration cannot possibly go below 1, give it max parallels
            } else {
                maxParallels = ParallelLogic.getParallelAmount(machine, recipe, Integer.MAX_VALUE);
            }

            return performOverclocking(EUt, recipe.duration, maxOverclockVoltage, maxParallels).asModifierFunction();
        }
        return ModifierFunction.IDENTITY;
    }

    /**
     * Determines the maximum number of overclocks that can be performed for a recipe.
     * Then performs overclocking on the Recipe.
     */
    default OCResult performOverclocking(long EUt, int duration, long maxOverclockVoltage, int maxParallels) {
        int recipeTier = GTUtil.getTierByVoltage(EUt);
        int maximumTier = GTUtil.getTierByVoltage(maxOverclockVoltage);
        // The maximum number of overclocks is determined by the difference between the tier the recipe is running at,
        // and the maximum tier that the machine can overclock to.
        int numberOfOCs = maximumTier - recipeTier;
        if (recipeTier == GTValues.ULV) numberOfOCs--; // no ULV overclocking

        // Always overclock even if numberOfOCs is <=0 as without it, some logic for coil bonuses ETC won't apply.
        OCParams params = new OCParams(EUt, duration, numberOfOCs, maxParallels);
        return runOverclockingLogic(params, maxOverclockVoltage);
    }

    /**
     * Standard overclocking algorithm with no sub-tick behavior.
     * <p>
     * While there are overclocks remaining:
     * <ol>
     * <li>Multiplies {@code EUt} by {@code voltageFactor}
     * <li>Multiplies {@code duration} by {@code durationFactor}
     * <li>Limit {@code duration} to {@code 1} tick, and stop overclocking early if needed
     *
     * @param params         the overclocking parameters
     * @param result         the result of the overclock
     * @param maxVoltage     the maximum voltage allowed to be overclocked to
     * @param durationFactor the factor to multiply duration by
     * @param voltageFactor  the factor to multiply voltage by
     */
    static OCResult standardOverclockingLogic(@NotNull OCParams params, long maxVoltage, double durationFactor,
                                              double voltageFactor) {
        double duration = params.duration;
        double eut = params.eut;
        int ocAmount = params.ocAmount;
        int ocLevel = 0;

        while (ocAmount-- > 0) {
            // it is important to do voltage first,
            // so overclocking voltage does not go above the limit before changing duration

            double potentialVoltage = eut * voltageFactor;
            // do not allow voltage to go above maximum
            if (potentialVoltage > maxVoltage) break;

            double potentialDuration = duration * durationFactor;
            // do not allow duration to go below one tick
            if (potentialDuration < 1) break;
            // update the duration for the next iteration
            duration = potentialDuration;

            // update the voltage for the next iteration after everything else
            // in case duration overclocking would waste energy
            eut = potentialVoltage;
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
     *
     * @param params         the overclocking parameters
     * @param result         the result of the overclock
     * @param maxVoltage     the maximum voltage allowed to be overclocked to
     * @param durationFactor the factor to multiply duration by
     * @param voltageFactor  the factor to multiply voltage by
     */
    static OCResult subTickNonParallelOC(@NotNull OCParams params, long maxVoltage, double durationFactor,
                                         double voltageFactor) {
        double duration = params.duration;
        double eut = params.eut;
        int ocAmount = params.ocAmount;
        int ocLevel = 0;
        double durationMultiplier = 1;

        while (ocAmount-- > 0) {
            double potentialEUt = eut * voltageFactor;
            if (potentialEUt > maxVoltage || potentialEUt < 1) break;

            double potentialDuration = duration * durationFactor;
            if (potentialDuration < 1) {
                potentialEUt = eut * durationFactor;
                if (potentialEUt > maxVoltage || potentialEUt < 1) break;
            } else {
                duration = potentialDuration;
                durationMultiplier *= durationFactor;
            }

            eut = potentialEUt;
            ocLevel++;
        }

        return new OCResult(Math.pow(voltageFactor, ocLevel), durationMultiplier, ocLevel, 1);
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
     *
     * @param params         the overclocking parameters
     * @param result         the result of the overclock
     * @param maxVoltage     the maximum voltage allowed to be overclocked to
     * @param durationFactor the factor to multiply duration by
     * @param voltageFactor  the factor to multiply voltage by
     */
    static OCResult subTickParallelOC(@NotNull OCParams params, long maxVoltage, double durationFactor,
                                      double voltageFactor) {
        double duration = params.duration;
        double eut = params.eut;
        int ocAmount = params.ocAmount;
        final int maxParallels = params.maxParallels;
        double parallel = 1;
        boolean shouldParallel = false;
        int ocLevel = 0;
        double durationMultiplier = 1;

        while (ocAmount-- > 0) {
            // it is important to do voltage first,
            // so overclocking voltage does not go above the limit before changing duration

            double potentialVoltage = eut * voltageFactor;
            // do not allow voltage to go above maximum
            if (potentialVoltage > maxVoltage) break;
            eut = potentialVoltage;

            if (shouldParallel) {
                double potentialParallel = parallel / durationFactor;
                if (potentialParallel > maxParallels) break;
                parallel = potentialParallel;
            } else {
                double potentialDuration = duration * durationFactor;
                if (potentialDuration < 1) {
                    double potentialParallel = parallel / durationFactor;
                    if (potentialParallel > maxParallels) break;
                    parallel = potentialParallel;
                    shouldParallel = true;
                } else {
                    duration = potentialDuration;
                    durationMultiplier *= durationFactor;
                }
            }
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
     *
     * @param params      the overclocking parameters
     * @param maxVoltage  the maximum voltage allowed to be overclocked to
     * @param recipeTemp  the temperature required by the recipe
     * @param machineTemp the provided temperature
     */
    static OCResult heatingCoilOC(@NotNull OCParams params, long maxVoltage, int recipeTemp, int machineTemp) {
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
            boolean perfect = perfectOCAmount-- > 0;

            double potentialEUt = eut * STD_VOLTAGE_FACTOR;
            if (potentialEUt > maxVoltage) break;
            eut = potentialEUt;

            if (shouldParallel) {
                double potentialParallel = parallel * (perfect ? PERFECT_DURATION_FACTOR_INV : STD_DURATION_FACTOR_INV);
                if (potentialParallel > maxParallels) break;
                parallel = potentialParallel;
            } else {
                double potentialDuration = duration * (perfect ? PERFECT_DURATION_FACTOR : STD_DURATION_FACTOR);
                if (potentialDuration < 1) {
                    double potentialParallel = parallel *
                            (perfect ? PERFECT_DURATION_FACTOR_INV : STD_DURATION_FACTOR_INV);
                    if (potentialParallel > maxParallels) break;
                    parallel = potentialParallel;
                    shouldParallel = true;
                } else {
                    duration = potentialDuration;
                    durationMultiplier *= perfect ? PERFECT_DURATION_FACTOR : STD_DURATION_FACTOR;
                }
            }
            ocLevel++;
        }

        return new OCResult(Math.pow(STD_VOLTAGE_FACTOR, ocLevel), durationMultiplier, ocLevel, (int) parallel);
    }

    /**
     * @param recipeTemp  the required temperature of the recipe
     * @param machineTemp the temperature provided by the machine
     * @return the amount of EU/t discounts to apply
     */
    private static int getCoilDiscountAmount(int recipeTemp, int machineTemp) {
        return Math.max(0, (machineTemp - recipeTemp) / COIL_EUT_DISCOUNT_TEMPERATURE);
    }

    /**
     * Handles applying the coil EU/t discount. Call before overclocking.
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

        public ModifierFunction asModifierFunction() {
            return ModifierFunction.builder()
                    .modifyAllContents(ContentModifier.multiplier(parallels))
                    .eutMultiplier(eutMultiplier)
                    .durationMultiplier(durationMultiplier)
                    .addOCs(ocLevel)
                    .parallels(parallels)
                    .build();
        }
    }
}
