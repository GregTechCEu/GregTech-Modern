package com.gregtechceu.gtceu.api.recipe.chance.boost;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import net.minecraft.util.Mth;

import org.jetbrains.annotations.NotNull;

/**
 * A function used to boost a {@link Content}'s chance
 */
@FunctionalInterface
public interface ChanceBoostFunction {

    /**
     * Chance boosting function based on the number of performed overclocks
     */
    ChanceBoostFunction OVERCLOCK = (entry, recipeTier, chanceTier) -> {
        int tierDiff = chanceTier - recipeTier;
        if (tierDiff <= 0) return entry.chance; // equal or invalid tiers do not boost at all
        if (recipeTier == GTValues.ULV) tierDiff--; // LV does not boost over ULV
        return Mth.clamp(entry.chance + (entry.tierChanceBoost * tierDiff), 0, entry.maxChance);
    };

    /**
     * Chance boosting function which performs no boosting
     */
    ChanceBoostFunction NONE = (entry, recipeTier, chanceTier) -> entry.chance;

    /**
     * @param entry      the amount to boost by
     * @param recipeTier the base tier of the recipe
     * @param chanceTier the tier the recipe is run at
     * @return the boosted chance
     */
    int getBoostedChance(@NotNull Content entry, int recipeTier, int chanceTier);
}
