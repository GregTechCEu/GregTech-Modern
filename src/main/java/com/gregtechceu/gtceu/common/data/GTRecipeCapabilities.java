package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.tterrag.registrate.util.entry.RegistryEntry;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class GTRecipeCapabilities {

    // spotless:off
    public final static RegistryEntry<RecipeCapability<?>, RecipeCapability<SizedIngredient>> ITEM = register(ItemRecipeCapability.CAP, "Item");
    public final static RegistryEntry<RecipeCapability<?>, RecipeCapability<SizedFluidIngredient>> FLUID = register(FluidRecipeCapability.CAP, "Fluid");
    public final static RegistryEntry<RecipeCapability<?>, RecipeCapability<BlockState>> BLOCK_STATE = register(BlockStateRecipeCapability.CAP, "Block State");
    public final static RegistryEntry<RecipeCapability<?>, RecipeCapability<EnergyStack>> EU = register(EURecipeCapability.CAP, "GTCEu Energy");
    public final static RegistryEntry<RecipeCapability<?>, RecipeCapability<Integer>> CWU = register(CWURecipeCapability.CAP, "Computation Work Units");
    //spotless:on

    public static void init() {}

    private static <T extends RecipeCapability<?>> RegistryEntry<RecipeCapability<?>, T> register(T cap, String name) {
        return REGISTRATE.generic(cap.id.getPath(), GTRegistries.Keys.RECIPE_CAPABILITY, () -> cap)
                .lang(v -> v.getId().toLanguageKey("recipe_capability"), name)
                .register();
    }
}
