package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;

import org.jetbrains.annotations.NotNull;

public interface IRangedIngredient {

    IntProvider getCountProvider();

    int getSampledCount();

    void setSampledCount(int count);

    /**
     * If this ingredient has not yet had its count rolled, rolls it and returns the roll.
     * If it has, returns the existing roll.
     * Passthrough method, invokes {@code rollSampledCount()} using the threadsafe {@link GTValues#RNG}.
     *
     * @return the amount rolled
     */
    default int rollSampledCount() {
        return rollSampledCount(GTValues.RNG);
    }

    int rollSampledCount(@NotNull RandomSource random);

    /**
     * @return the average roll of this ranged amount
     */
    default double getMidRoll() {
        return ((getCountProvider().getMaxValue() + getCountProvider().getMinValue()) / 2.0);
    }

    default boolean isRolled() {
        return getSampledCount() != -1;
    }

    void reset();
}
