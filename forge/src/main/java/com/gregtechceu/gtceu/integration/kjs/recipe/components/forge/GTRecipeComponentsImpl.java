package com.gregtechceu.gtceu.integration.kjs.recipe.components.forge;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities;
import com.gregtechceu.gtceu.common.data.forge.GTRecipeCapabilitiesImpl;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.ContentJS;
import com.lowdragmc.lowdraglib.LDLib;
import com.mojang.datafixers.util.Pair;
import dev.latvian.kubejs.mekanism.recipe.MekComponents;
import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.fluid.OutputFluid;
import dev.latvian.mods.kubejs.recipe.component.FluidComponents;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;

import java.util.Map;

public class GTRecipeComponentsImpl {

    public static final ContentJS<ChemicalStackIngredient.GasStackIngredient> GAS_IN;
    public static final ContentJS<GasStack> GAS_OUT;

    public static void registerPlatformCaps(Map<RecipeCapability<?>, Pair<ContentJS<?>, ContentJS<?>>> validCaps) {
        if (LDLib.isModLoaded(GTValues.MODID_KUBEJS_MEKANISM)) {
            validCaps.put(GTRecipeCapabilitiesImpl.GAS, Pair.of(GAS_IN, GAS_OUT));
        }
    }

    static {
        if (LDLib.isModLoaded(GTValues.MODID_KUBEJS_MEKANISM)) {
            GAS_IN = new ContentJS<>(MekComponents.GAS_INPUT, GTRecipeCapabilitiesImpl.GAS, false);
            GAS_OUT = new ContentJS<>(MekComponents.GAS_OUTPUT, GTRecipeCapabilitiesImpl.GAS, true);
        } else {
            GAS_IN = null;
            GAS_OUT = null;
        }
    }
}
