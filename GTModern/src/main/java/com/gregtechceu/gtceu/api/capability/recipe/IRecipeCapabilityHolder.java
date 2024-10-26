package com.gregtechceu.gtceu.api.capability.recipe;

import com.google.common.collect.Table;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IRecipeCapabilityHolder {

    default boolean hasProxies() {
        return !getCapabilitiesProxy().isEmpty();
    }

    @NotNull
    Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> getCapabilitiesProxy();

    /**
     * get Tier for chance boost.
     * if -1, all chanced outputs are voided.
     */
    default int getChanceTier() {
        return 0;
    }
}
