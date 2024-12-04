package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.recipe.logic.OCParams;
import com.gregtechceu.gtceu.api.recipe.logic.OCResult;
import com.gregtechceu.gtceu.utils.GTUtil;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * A class for holding all the various Overclocking logics
 */
public class OverclockingLogic {

    @FunctionalInterface
    public interface Logic {

        /**
         * Calls the desired overclocking logic to be run for the recipe.
         * Performs the actual overclocking on the provided recipe.
         * Override this to call custom overclocking mechanics
         *
         * @param ocParams   the parameters for the overclock
         * @param ocResult   the result to store the overclock in
         * @param maxVoltage the maximum voltage the recipe is allowed to be run at
         */
        void runOverclockingLogic(@NotNull OCParams ocParams, @NotNull OCResult ocResult, long maxVoltage);
    }

    public static final double STD_VOLTAGE_FACTOR = 4.0;
    public static final double PERFECT_HALF_VOLTAGE_FACTOR = 2.0;
    public static final double STD_DURATION_FACTOR = 0.5;
    public static final double STD_DURATION_FACTOR_INV = 2.0;
    public static final double PERFECT_DURATION_FACTOR = 0.25;
    public static final double PERFECT_DURATION_FACTOR_INV = 4.0;
    public static final double PERFECT_HALF_DURATION_FACTOR = 0.5;
    public static final double PERFECT_HALF_DURATION_FACTOR_INV = 2.0;
    public static final int COIL_EUT_DISCOUNT_TEMPERATURE = 900;

    public static final OverclockingLogic PERFECT_OVERCLOCK = new OverclockingLogic(PERFECT_DURATION_FACTOR,
            STD_VOLTAGE_FACTOR, false);
    public static final OverclockingLogic NON_PERFECT_OVERCLOCK = new OverclockingLogic(
            STD_DURATION_FACTOR, STD_VOLTAGE_FACTOR, false);

    public static final OverclockingLogic PERFECT_OVERCLOCK_SUBTICK = new OverclockingLogic(PERFECT_DURATION_FACTOR,
            STD_VOLTAGE_FACTOR, true);
    public static final OverclockingLogic NON_PERFECT_OVERCLOCK_SUBTICK = new OverclockingLogic(
            STD_DURATION_FACTOR, STD_VOLTAGE_FACTOR, true);

    @Getter
    protected Logic logic;

    public OverclockingLogic(Logic logic) {
        this.logic = logic;
    }

    public OverclockingLogic(double durationFactor, double voltageFactor, boolean subtick) {
        if (subtick) {
            this.logic = (ocParams, ocResult, maxVoltage) -> subTickParallelOC(
                    ocParams,
                    ocResult,
                    maxVoltage,
                    durationFactor,
                    voltageFactor);
        } else {
            this.logic = (ocParams, ocResult, maxVoltage) -> standardOverclockingLogic(
                    ocParams,
                    ocResult,
                    maxVoltage,
                    durationFactor,
                    voltageFactor);
        }
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
    public static void standardOverclockingLogic(@NotNull OCParams params, @NotNull OCResult result, long maxVoltage,
                                                 double durationFactor,
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
        result.init((long) eut, (int) duration, ocLevel);
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
    public static void subTickNonParallelOC(@NotNull OCParams params, @NotNull OCResult result, long maxVoltage,
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
    public static void subTickParallelOC(@NotNull OCParams params, @NotNull OCResult result,
                                         long maxVoltage, double durationFactor, double voltageFactor) {
        double duration = params.getDuration();
        double eut = params.getEut();
        int ocAmount = params.getOcAmount();
        double parallel = 1;
        int parallelIterAmount = 0;
        boolean shouldParallel = false;
        int ocLevel = 0;

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
                }
            }
            ocLevel++;
        }
        result.init((long) (eut / Math.pow(voltageFactor, parallelIterAmount)), (int) duration, (int) parallel,
                (long) eut, ocLevel);
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
    public static void heatingCoilOC(@NotNull OCParams params, @NotNull OCResult result, long maxVoltage,
                                     int providedTemp, int requiredTemp) {
        int perfectOCAmount = calculateAmountCoilEUtDiscount(providedTemp, requiredTemp) / 2;
        double duration = params.getDuration();
        double eut = params.getEut();
        int ocAmount = params.getOcAmount();
        double parallel = 1;
        int parallelIterAmount = 0;
        boolean shouldParallel = false;
        int ocLevel = 0;

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
                }
            }
            ocLevel++;
        }

        result.init((long) (eut / Math.pow(STD_VOLTAGE_FACTOR, parallelIterAmount)), (int) duration, (int) parallel,
                (long) eut, ocLevel);
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
    public static void heatingCoilNonSubTickOC(@NotNull OCParams params, @NotNull OCResult result, long maxVoltage,
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
    public static long applyCoilEUtDiscount(long recipeEUt, int providedTemp, int requiredTemp) {
        if (requiredTemp < COIL_EUT_DISCOUNT_TEMPERATURE) return recipeEUt;
        int amountEUtDiscount = calculateAmountCoilEUtDiscount(providedTemp, requiredTemp);
        if (amountEUtDiscount < 1) return recipeEUt;
        return (long) (recipeEUt * Math.min(1, Math.pow(0.95, amountEUtDiscount)));
    }

    /**
     * Finds the maximum tier that a recipe can overclock to, when provided the maximum voltage a recipe can overclock
     * to.
     *
     * @param voltage The maximum voltage the recipe is allowed to overclock to.
     * @return the highest voltage tier the machine should use to overclock with
     */
    protected int getOverclockForTier(long voltage) {
        return GTUtil.getTierByVoltage(voltage);
    }
}
