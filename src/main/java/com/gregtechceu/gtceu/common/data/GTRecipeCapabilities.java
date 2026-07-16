package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GTRecipeCapabilities {

    private static final DeferredRegister<RecipeCapability<?>> RECIPE_CAPABILITY = DeferredRegister
            .create(GTRegistries.Keys.RECIPE_CAPABILITY, GTCEu.MOD_ID);

    public final static RecipeCapability<SizedIngredient> ITEM = ItemRecipeCapability.CAP;
    public final static RecipeCapability<SizedFluidIngredient> FLUID = FluidRecipeCapability.CAP;
    public final static RecipeCapability<BlockState> BLOCK_STATE = BlockStateRecipeCapability.CAP;
    public final static RecipeCapability<EnergyStack> EU = EURecipeCapability.CAP;
    public final static RecipeCapability<Integer> CWU = CWURecipeCapability.CAP;

    public static void init(IEventBus modBus) {
        RECIPE_CAPABILITY.register(modBus);

        RECIPE_CAPABILITY.register(ITEM.id.getPath(), () -> ITEM);
        RECIPE_CAPABILITY.register(FLUID.id.getPath(), () -> FLUID);
        RECIPE_CAPABILITY.register(BLOCK_STATE.id.getPath(), () -> BLOCK_STATE);
        RECIPE_CAPABILITY.register(EU.id.getPath(), () -> EU);
        RECIPE_CAPABILITY.register(CWU.id.getPath(), () -> CWU);
    }
}
