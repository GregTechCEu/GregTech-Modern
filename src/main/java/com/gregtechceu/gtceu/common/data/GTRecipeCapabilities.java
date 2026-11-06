package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.tterrag.registrate.util.entry.RegistryEntry;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class GTRecipeCapabilities {

    // spotless:off
    public final static RegistryEntry<RecipeCapability<?>, ItemRecipeCapability> ITEM = register(ItemRecipeCapability.CAP, "Item");
    public final static RegistryEntry<RecipeCapability<?>, FluidRecipeCapability> FLUID = register(FluidRecipeCapability.CAP, "Fluid");
    public final static RegistryEntry<RecipeCapability<?>, BlockStateRecipeCapability> BLOCK_STATE = register(BlockStateRecipeCapability.CAP, "Block State");
    public final static RegistryEntry<RecipeCapability<?>, EURecipeCapability> EU = register(EURecipeCapability.CAP, "GTCEu Energy");
    public final static RegistryEntry<RecipeCapability<?>, CWURecipeCapability> CWU = register(CWURecipeCapability.CAP, "Computation Work Units");
    //spotless:on

    public static void init() {}

    private static <T extends RecipeCapability<?>> RegistryEntry<RecipeCapability<?>, T> register(T cap, String name) {
        return REGISTRATE.generic(cap.id.getPath(), GTRegistries.Keys.RECIPE_CAPABILITY, () -> cap)
                .lang(v -> v.getId().toLanguageKey("recipe_capability"), name)
                .register();
    }
}
