package com.gregtechceu.gtceu.api.capability.recipe;

import com.google.common.collect.Table;

import javax.annotation.Nonnull;
import java.util.List;


public interface IRecipeCapabilityHolder {

    default boolean hasProxies() {
        return !getCapabilitiesProxy().isEmpty() && !getCapabilitiesProxy().isEmpty();
    }

    @Nonnull
    Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> getCapabilitiesProxy();

    /**
     * get Tier for chance boost.
     */
    default int getChanceTier() {
        return 0;
    }

}
