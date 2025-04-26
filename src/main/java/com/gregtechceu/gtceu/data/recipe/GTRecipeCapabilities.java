package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class GTRecipeCapabilities {

    public final static RecipeCapability<SizedIngredient> ITEM = ItemRecipeCapability.CAP;
    public final static RecipeCapability<SizedFluidIngredient> FLUID = FluidRecipeCapability.CAP;
    public final static RecipeCapability<BlockState> BLOCK_STATE = BlockStateRecipeCapability.CAP;
    public final static RecipeCapability<Long> EU = EURecipeCapability.CAP;
    public final static RecipeCapability<Integer> CWU = CWURecipeCapability.CAP;

    public static void init() {
        GTRegistries.register(GTRegistries.RECIPE_CAPABILITIES, GTCEu.id(ITEM.name), ITEM);
        GTRegistries.register(GTRegistries.RECIPE_CAPABILITIES, GTCEu.id(FLUID.name), FLUID);
        GTRegistries.register(GTRegistries.RECIPE_CAPABILITIES, GTCEu.id(BLOCK_STATE.name), BLOCK_STATE);
        GTRegistries.register(GTRegistries.RECIPE_CAPABILITIES, GTCEu.id(EU.name), EU);
        GTRegistries.register(GTRegistries.RECIPE_CAPABILITIES, GTCEu.id(CWU.name), CWU);
    }
}
