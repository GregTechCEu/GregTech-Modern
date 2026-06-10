package com.gregtechceu.gtceu.api.recipe.chance.boost;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.util.Mth;

/**
 * A function used to boost a recipe content chance.
 */
@FunctionalInterface
public interface ChanceBoostFunction {

    /**
     * Chance boosting function based on the number of performed overclocks
     */
    ChanceBoostFunction OVERCLOCK = (chance, recipeTier, chanceTier) -> {
        int tierDiff = chanceTier - recipeTier;
        if (tierDiff <= 0) return chance; // equal or invalid tiers do not boost at all
        if (recipeTier == GTValues.ULV) tierDiff--; // LV does not boost over ULV
        return Mth.clamp(chance, 0, 10000);
    };

    /**
     * Chance boosting function which performs no boosting
     */
    ChanceBoostFunction NONE = (chance, recipeTier, chanceTier) -> chance;

    /**
     * @param chance     the base chance
     * @param recipeTier the base tier of the recipe
     * @param chanceTier the tier the recipe is run at
     * @return the boosted chance
     */
    int getBoostedChance(int chance, int recipeTier, int chanceTier);
}
