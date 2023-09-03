package com.gregtechceu.gtceu.common.data.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.forge.GasRecipeCapability;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import mekanism.api.chemical.gas.GasStack;

public class GTRecipeCapabilitiesImpl {

    public final static RecipeCapability<GasStack> GAS = GasRecipeCapability.CAP;

    public static void initPlatform() {
        if (GTCEu.isMekanismLoaded()) {
            GTRegistries.RECIPE_CAPABILITIES.register(GAS.name, GAS);
        }
    }
}
