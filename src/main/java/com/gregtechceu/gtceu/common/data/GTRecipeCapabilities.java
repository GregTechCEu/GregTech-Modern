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
    public final static RegistryEntry<RecipeCapability<?>, RecipeCapability<SizedIngredient>> ITEM = register(ItemRecipeCapability.CAP);
    public final static RegistryEntry<RecipeCapability<?>, RecipeCapability<SizedFluidIngredient>> FLUID = register(FluidRecipeCapability.CAP);
    public final static RegistryEntry<RecipeCapability<?>, RecipeCapability<BlockState>> BLOCK_STATE = register(BlockStateRecipeCapability.CAP);
    public final static RegistryEntry<RecipeCapability<?>, RecipeCapability<EnergyStack>> EU = register(EURecipeCapability.CAP);
    public final static RegistryEntry<RecipeCapability<?>, RecipeCapability<Integer>> CWU = register(CWURecipeCapability.CAP);
    //spotless:on

    public static void init() {}

    private static <T extends RecipeCapability<?>> RegistryEntry<RecipeCapability<?>, T> register(T cap) {
        return REGISTRATE.simple(cap.id.getPath(), GTRegistries.Keys.RECIPE_CAPABILITY, () -> cap);
    }
}
