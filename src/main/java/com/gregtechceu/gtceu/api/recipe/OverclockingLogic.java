package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.logic.OCParams;
import com.gregtechceu.gtceu.api.recipe.logic.OCResult;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.utils.GTUtil;

import org.jetbrains.annotations.NotNull;

/**
 * A class for holding all the various Overclocking logics
 */
@FunctionalInterface
public interface OverclockingLogic {

    ModifierFunction runOverclockingLogic(@NotNull OCParams ocParams, long maxVoltage);

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

    /**
     * Calculates the overclocked Recipe's final duration and EU/t
     *
     * @param recipe the recipe to run
     * @return a new recipe
     */
    default ModifierFunction applyOverclock(@NotNull GTRecipe recipe, long maxOverclockVoltage) {
        long EUt = RecipeHelper.getRealEUt(recipe);
        if (EUt > 0) return performOverclocking(recipe.duration, EUt, maxOverclockVoltage);
        return ModifierFunction.IDENTITY;
    }

    /**
     * Determines the maximum number of overclocks that can be performed for a recipe.
     * Then performs overclocking on the Recipe.
     *
     * @param recipe the recipe to overclock
     */
    default ModifierFunction performOverclocking(int duration, long EUt, long maxOverclockVoltage) {
        int recipeTier = GTUtil.getTierByVoltage(Math.abs(EUt));
        int maximumTier = GTUtil.getTierByVoltage(maxOverclockVoltage);
        // The maximum number of overclocks is determined by the difference between the tier the recipe is running at,
        // and the maximum tier that the machine can overclock to.
        int numberOfOCs = maximumTier - recipeTier;
        if (recipeTier == GTValues.ULV) numberOfOCs--; // no ULV overclocking

        // Always overclock even if numberOfOCs is <=0 as without it, some logic for coil bonuses ETC won't apply.
        OCParams params = new OCParams(EUt, duration, numberOfOCs);
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
    static ModifierFunction standardOverclockingLogic(@NotNull OCParams params, long maxVoltage, double durationFactor,
                                                      double voltageFactor) {
        double duration = params.getDuration();
        double eut = params.getEut();
        int ocAmount = params.getOcAmount();
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

        return ModifierFunction.builder()
                .durationModifier(ContentModifier.multiplier(Math.pow(durationFactor, ocLevel)))
                .eutModifier(ContentModifier.multiplier(Math.pow(voltageFactor, ocLevel)))
                .addOCs(ocLevel)
                .build();
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
    static void subTickNonParallelOC(@NotNull OCParams params, @NotNull OCResult result, long maxVoltage,
                                     double durationFactor,
                                     double voltageFactor) {
        double duration = params.getDuration();
        double eut = params.getEut();
        int ocAmount = params.getOcAmount();
        int ocLevel = 0;

        while (ocAmount-- > 0) {
            double potentialEUt = eut * voltageFactor;
            if (potentialEUt > maxVoltage || potentialEUt < 1) break;

            double potentialDuration = duration * durationFactor;
            if (potentialDuration < 1) {
                potentialEUt = eut * durationFactor;
                if (potentialEUt > maxVoltage || potentialEUt < 1) break;
            } else {
                duration = potentialDuration;
            }

            eut = potentialEUt;
            ocLevel++;
        }

        result.init((long) eut, (int) duration, ocLevel);
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
    static ModifierFunction subTickParallelOC(@NotNull OCParams params, long maxVoltage, double durationFactor,
                                              double voltageFactor) {
        double duration = params.getDuration();
        double eut = params.getEut();
        int ocAmount = params.getOcAmount();
        double parallel = 1;
        int parallelIterAmount = 0;
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
                parallel /= durationFactor;
                parallelIterAmount++;
            } else {
                double potentialDuration = duration * durationFactor;
                if (potentialDuration < 1) {
                    parallel /= durationFactor;
                    parallelIterAmount++;
                    shouldParallel = true;
                } else {
                    duration = potentialDuration;
                    durationMultiplier *= durationFactor;
                }
            }
            ocLevel++;
        }

        // start eut
        // after loop: eut = eut * voltagefactor^oc
        // result: eut = eut / voltagefactor^paralleliter

        // start -> result: eut = eut * vf^oc / vf^iter

        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier((int) parallel))
                .eutModifier(ContentModifier.multiplier(Math.pow(voltageFactor, ocLevel - parallelIterAmount)))
                .durationModifier(ContentModifier.multiplier(durationMultiplier))
                .multiplyParallels((int) parallel)
                .addOCs(ocLevel)
                .build();

        // result.init((long) (eut / Math.pow(voltageFactor, parallelIterAmount)), (int) duration, (int) parallel,
        // (long) eut, ocLevel);
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
     * <li>The maximum amount of perfect OCs is determined by {@link #calculateAmountCoilEUtDiscount(int, int)}, divided
     * by 2.
     *
     * @param params       the overclocking parameters
     * @param result       the result of the overclock
     * @param maxVoltage   the maximum voltage allowed to be overclocked to
     * @param providedTemp the provided temperature
     * @param requiredTemp the temperature required by the recipe
     */
    static ModifierFunction heatingCoilOC(@NotNull OCParams params, long maxVoltage, int providedTemp,
                                          int requiredTemp) {
        int perfectOCAmount = calculateAmountCoilEUtDiscount(providedTemp, requiredTemp) / 2;
        double duration = params.getDuration();
        double eut = params.getEut();
        int ocAmount = params.getOcAmount();
        double parallel = 1;
        int parallelIterAmount = 0;
        boolean shouldParallel = false;
        int ocLevel = 0;

        double durationMultiplier = 1;

        while (ocAmount-- > 0) {
            boolean perfect = perfectOCAmount-- > 0;

            double potentialEUt = eut * STD_VOLTAGE_FACTOR;
            if (potentialEUt > maxVoltage) break;
            eut = potentialEUt;

            if (shouldParallel) {
                if (perfect) {
                    parallel *= PERFECT_DURATION_FACTOR_INV;
                } else {
                    parallel *= STD_DURATION_FACTOR_INV;
                }
                parallelIterAmount++;
            } else {
                double potentialDuration;
                if (perfect) {
                    potentialDuration = duration * PERFECT_DURATION_FACTOR;
                } else {
                    potentialDuration = duration * STD_DURATION_FACTOR;
                }

                if (potentialDuration < 1) {
                    if (perfect) {
                        parallel *= PERFECT_DURATION_FACTOR_INV;
                    } else {
                        parallel *= STD_DURATION_FACTOR_INV;
                    }

                    parallelIterAmount++;
                    shouldParallel = true;
                } else {
                    duration = potentialDuration;
                    durationMultiplier *= perfect ? PERFECT_DURATION_FACTOR : STD_DURATION_FACTOR;
                }
            }
            ocLevel++;
        }

        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier((int) parallel))
                .eutModifier(ContentModifier.multiplier(Math.pow(STD_VOLTAGE_FACTOR, ocLevel - parallelIterAmount)))
                .durationModifier(ContentModifier.multiplier(durationMultiplier))
                .multiplyParallels((int) parallel)
                .addOCs(ocLevel)
                .build();

        // result.init((long) (eut / Math.pow(STD_VOLTAGE_FACTOR, parallelIterAmount)), (int) duration, (int) parallel,
        // (long) eut, ocLevel);
    }

    /**
     * Heating Coil overclocking algorithm without sub-tick parallelization.
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
     * <li>The maximum amount of perfect OCs is determined by {@link #calculateAmountCoilEUtDiscount(int, int)}, divided
     * by 2.
     *
     * @param params       the overclocking parameters
     * @param result       the result of the overclock
     * @param maxVoltage   the maximum voltage allowed to be overclocked to
     * @param providedTemp the provided temperature
     * @param requiredTemp the temperature required by the recipe
     */
    static void heatingCoilNonSubTickOC(@NotNull OCParams params, @NotNull OCResult result, long maxVoltage,
                                        int providedTemp, int requiredTemp) {
        int amountPerfectOC = calculateAmountCoilEUtDiscount(providedTemp, requiredTemp) / 2;
        double duration = params.getDuration();
        double eut = params.getEut();
        double ocAmount = params.getOcAmount();
        int ocLevel = 0;

        while (ocAmount-- > 0) {
            boolean perfect = amountPerfectOC-- > 0;

            double potentialEUt = eut * STD_VOLTAGE_FACTOR;
            if (potentialEUt > maxVoltage) {
                break;
            }
            eut = potentialEUt;

            double potentialDuration;
            if (perfect) {
                potentialDuration = duration * PERFECT_DURATION_FACTOR;
            } else {
                potentialDuration = duration * STD_DURATION_FACTOR;
            }
            if (potentialDuration < 1) {
                break;
            }
            duration = potentialDuration;
            ocLevel++;
        }
        result.init((long) eut, (int) duration, ocLevel);
    }

    /**
     * @param providedTemp the temperate provided by the machine
     * @param requiredTemp the required temperature of the recipe
     * @return the amount of EU/t discounts to apply
     */
    private static int calculateAmountCoilEUtDiscount(int providedTemp, int requiredTemp) {
        return Math.max(0, (providedTemp - requiredTemp) / COIL_EUT_DISCOUNT_TEMPERATURE);
    }

    /**
     * Handles applying the coil EU/t discount. Call before overclocking.
     *
     * @param recipeEUt    the EU/t of the recipe
     * @param providedTemp the temperate provided by the machine
     * @param requiredTemp the required temperature of the recipe
     * @return the discounted EU/t
     */
    static double getCoilEUtDiscount(int providedTemp, int requiredTemp) {
        if (requiredTemp < COIL_EUT_DISCOUNT_TEMPERATURE) return 1;
        int amountEUtDiscount = calculateAmountCoilEUtDiscount(providedTemp, requiredTemp);
        if (amountEUtDiscount < 1) return 1;
        return Math.min(1, Math.pow(0.95, amountEUtDiscount));
    }
}
