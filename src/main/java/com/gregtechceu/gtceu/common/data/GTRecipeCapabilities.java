package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModLoader;

public class GTRecipeCapabilities {

    public final static RecipeCapability<Ingredient> ITEM = ItemRecipeCapability.CAP;
    public final static RecipeCapability<FluidIngredient> FLUID = FluidRecipeCapability.CAP;
    public final static RecipeCapability<BlockState> BLOCK_STATE = BlockStateRecipeCapability.CAP;
    public final static RecipeCapability<EnergyStack> EU = EURecipeCapability.CAP;
    public final static RecipeCapability<Integer> CWU = CWURecipeCapability.CAP;

    public static void init() {
        GTRegistries.register(GTRegistries.RECIPE_CAPABILITIES, ITEM.id, ITEM);
        GTRegistries.register(GTRegistries.RECIPE_CAPABILITIES, FLUID.id, FLUID);
        GTRegistries.register(GTRegistries.RECIPE_CAPABILITIES, BLOCK_STATE.id, BLOCK_STATE);
        GTRegistries.register(GTRegistries.RECIPE_CAPABILITIES, EU.id, EU);
        GTRegistries.register(GTRegistries.RECIPE_CAPABILITIES, CWU.id, CWU);
    }
}
