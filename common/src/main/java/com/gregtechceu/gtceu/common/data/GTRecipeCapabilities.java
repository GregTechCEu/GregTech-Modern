package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote GTRecipeCapabilities
 */
public class GTRecipeCapabilities {

    public final static RecipeCapability<Ingredient> ITEM = ItemRecipeCapability.CAP;
    public final static RecipeCapability<FluidIngredient> FLUID = FluidRecipeCapability.CAP;
    public final static RecipeCapability<BlockState> BLOCK_STATE = BlockStateRecipeCapability.CAP;
    public final static RecipeCapability<Long> EU = EURecipeCapability.CAP;
    public final static RecipeCapability<Float> SU = StressRecipeCapability.CAP;

    public static void init() {
        GTRegistries.RECIPE_CAPABILITIES.register(ITEM.name, ITEM);
        GTRegistries.RECIPE_CAPABILITIES.register(FLUID.name, FLUID);
        GTRegistries.RECIPE_CAPABILITIES.register(BLOCK_STATE.name, BLOCK_STATE);
        GTRegistries.RECIPE_CAPABILITIES.register(EU.name, EU);
        GTRegistries.RECIPE_CAPABILITIES.register(SU.name, SU);
    }
}
