package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.api.GTValues;

public interface IChancedIngredient {

    int GAUSSIAN_ROLL_THRESHOLD = 16;

    int MAX_CHANCE = 10000;

    static int rollSuccesses(int times, int chance) {
        if (times <= 0 || chance <= 0) return 0;
        if (chance >= MAX_CHANCE) return times;

        if (times <= GAUSSIAN_ROLL_THRESHOLD) {
            int successes = 0;
            for (int i = 0; i < times; i++) {
                if (GTValues.RNG.nextInt(MAX_CHANCE) < chance) successes++;
            }
            return successes;
        }

        double probability = (double) chance / MAX_CHANCE;
        double mean = times * probability;
        double deviation = Math.sqrt(times * probability * (1.0 - probability));
        int successes = (int) Math.round(mean + GTValues.RNG.nextGaussian() * deviation);
        return Math.max(0, Math.min(times, successes));
    }
}
