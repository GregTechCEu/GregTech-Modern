package com.gregtechceu.gtceu.data.tag;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;

import com.gregtechceu.gtceu.api.recipe.ingredient.SizedSingleFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedTagFluidIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class GTIngredientTypes {

    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.INGREDIENT_TYPES, GTCEu.MOD_ID);
    public static final DeferredRegister<FluidIngredientType<?>> FLUID_INGREDIENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.FLUID_INGREDIENT_TYPES, GTCEu.MOD_ID);

    public static final DeferredHolder<IngredientType<?>, IngredientType<IntCircuitIngredient>> INT_CIRCUIT_INGREDIENT = INGREDIENT_TYPES
            .register("circuit", () -> new IngredientType<>(IntCircuitIngredient.CODEC));


    public static final DeferredHolder<FluidIngredientType<?>, FluidIngredientType<SizedTagFluidIngredient>> SIZED_TAG_FLUID_INGREDIENT =
            FLUID_INGREDIENT_TYPES.register("sized_tag", () -> new FluidIngredientType<>(SizedTagFluidIngredient.CODEC));
    public static final DeferredHolder<FluidIngredientType<?>, FluidIngredientType<SizedSingleFluidIngredient>> SIZED_SINGLE_FLUID_INGREDIENT =
            FLUID_INGREDIENT_TYPES.register("sized_single", () -> new FluidIngredientType<>(SizedSingleFluidIngredient.CODEC));
}
