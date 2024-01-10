package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.mojang.datafixers.util.Function5;
import it.unimi.dsi.fastutil.longs.LongIntMutablePair;
import it.unimi.dsi.fastutil.longs.LongIntPair;
import lombok.Getter;

import javax.annotation.Nonnull;

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
         * @param recipe          current recipe
         * @param recipeEUt       the EUt of the recipe
         * @param maxVoltage      the maximum voltage the recipe is allowed to be run at
         * @param duration        the duration of the recipe
         * @param amountOC        the maximum amount of overclocks to perform
         * @return an int array of {OverclockedEUt, OverclockedDuration}
         */
        LongIntPair runOverclockingLogic(@Nonnull GTRecipe recipe, long recipeEUt, long maxVoltage, int duration, int amountOC);
    }

    public static final double STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER = 4.0;
    public static final double STANDARD_OVERCLOCK_DURATION_DIVISOR = ConfigHolder.INSTANCE.machines.overclockDivisor;
    public static final double PERFECT_OVERCLOCK_DURATION_DIVISOR = 4.0;

    public static final OverclockingLogic PERFECT_OVERCLOCK = new OverclockingLogic(PERFECT_OVERCLOCK_DURATION_DIVISOR, STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER);
    public static final OverclockingLogic NON_PERFECT_OVERCLOCK = new OverclockingLogic(STANDARD_OVERCLOCK_DURATION_DIVISOR, STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER);

    @Getter
    protected Logic logic;

    public OverclockingLogic(Logic logic) {
        this.logic = logic;
    }

    public OverclockingLogic(double durationDivisor, double voltageMultiplier) {
        this.logic = (recipe, recipeEUt, maxVoltage, duration, amountOC) -> standardOverclockingLogic(
                Math.abs(recipeEUt),
                maxVoltage,
                duration,
                amountOC,
                durationDivisor,
                voltageMultiplier
        );
    }

    /**
     * applies standard logic for overclocking, where each overclock modifies energy and duration
     *
     * @param recipeEUt         the EU/t of the recipe to overclock
     * @param maxVoltage        the maximum voltage the recipe is allowed to be run at
     * @param recipeDuration    the duration of the recipe to overclock
     * @param durationDivisor   the value to divide the duration by for each overclock
     * @param voltageMultiplier the value to multiply the voltage by for each overclock
     * @param numberOfOCs       the maximum amount of overclocks allowed
     * @return an int array of {OverclockedEUt, OverclockedDuration}
     */
    @Nonnull
    public static LongIntPair standardOverclockingLogic(long recipeEUt, long maxVoltage, int recipeDuration, int numberOfOCs, double durationDivisor, double voltageMultiplier) {
        double resultDuration = recipeDuration;
        double resultVoltage = recipeEUt;

        for (; numberOfOCs > 0; numberOfOCs--) {
            // it is important to do voltage first,
            // so overclocking voltage does not go above the limit before changing duration

            double potentialVoltage = resultVoltage * voltageMultiplier;
            // do not allow voltage to go above maximum
            if (potentialVoltage > maxVoltage) break;

            double potentialDuration = resultDuration / durationDivisor;
            // do not allow duration to go below one tick
            if (potentialDuration < 1) break;
            // update the duration for the next iteration
            resultDuration = potentialDuration;

            // update the voltage for the next iteration after everything else
            // in case duration overclocking would waste energy
            resultVoltage = potentialVoltage;
        }
        return LongIntMutablePair.of((long) resultVoltage, (int) resultDuration);
    }

    @Nonnull
    public static LongIntPair heatingCoilOverclockingLogic(long recipeEUt, long maximumVoltage, int recipeDuration, int maxOverclocks, int currentTemp, int recipeRequiredTemp) {
        int amountEUDiscount = Math.max(0, (currentTemp - recipeRequiredTemp) / 900);
        int amountPerfectOC = amountEUDiscount / 2;

        // apply a multiplicative 95% energy multiplier for every 900k over recipe temperature
        recipeEUt *= Math.min(1, Math.pow(0.95, amountEUDiscount));

        // perfect overclock for every 1800k over recipe temperature
        if (amountPerfectOC > 0) {
            // use the normal overclock logic to do perfect OCs up to as many times as calculated
            var overclock = standardOverclockingLogic(recipeEUt, maximumVoltage, recipeDuration, amountPerfectOC, PERFECT_OVERCLOCK_DURATION_DIVISOR, STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER);

            // overclock normally as much as possible after perfects are exhausted
            return standardOverclockingLogic(overclock.leftLong(), maximumVoltage, overclock.rightInt(), maxOverclocks - amountPerfectOC, STANDARD_OVERCLOCK_DURATION_DIVISOR, STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER);
        }

        // no perfects are performed, do normal overclocking
        return standardOverclockingLogic(recipeEUt, maximumVoltage, recipeDuration, maxOverclocks, STANDARD_OVERCLOCK_DURATION_DIVISOR, STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER);
    }

    /**
     * Finds the maximum tier that a recipe can overclock to, when provided the maximum voltage a recipe can overclock to.
     *
     * @param voltage The maximum voltage the recipe is allowed to overclock to.
     * @return the highest voltage tier the machine should use to overclock with
     */
    protected int getOverclockForTier(long voltage) {
        return GTUtil.getTierByVoltage(voltage);
    }

}
