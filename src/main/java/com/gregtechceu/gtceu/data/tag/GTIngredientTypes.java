package com.gregtechceu.gtceu.data.tag;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.ingredient.*;

import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class GTIngredientTypes {

    public static final DeferredRegister<IngredientType<?>> ITEM_INGREDIENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.INGREDIENT_TYPES, GTCEu.MOD_ID);
    public static final DeferredRegister<FluidIngredientType<?>> FLUID_INGREDIENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.FLUID_INGREDIENT_TYPES, GTCEu.MOD_ID);

    public static final DeferredHolder<IngredientType<?>, IngredientType<IntCircuitIngredient>> INT_CIRCUIT_INGREDIENT = ITEM_INGREDIENT_TYPES
            .register("circuit", () -> new IngredientType<>(IntCircuitIngredient.CODEC));

    public static final DeferredHolder<IngredientType<?>, IngredientType<IntProviderIngredient>> INT_PROVIDER_INGREDIENT = ITEM_INGREDIENT_TYPES
            .register("int_provider", () -> new IngredientType<>(IntProviderIngredient.CODEC));

    public static final DeferredHolder<IngredientType<?>, IngredientType<FluidContainerIngredient>> FLUID_CONTAINER_INGREDIENT = ITEM_INGREDIENT_TYPES
            .register("fluid_container", () -> new IngredientType<>(FluidContainerIngredient.CODEC));

    public static final DeferredHolder<IngredientType<?>, IngredientType<ExDataComponentIngredient>> DATA_COMPONENT_INGREDIENT = ITEM_INGREDIENT_TYPES
            .register("components", () -> new IngredientType<>(ExDataComponentIngredient.CODEC));

    public static final DeferredHolder<FluidIngredientType<?>, FluidIngredientType<ExDataComponentFluidIngredient>> DATA_COMPONENT_FLUID_INGREDIENT = FLUID_INGREDIENT_TYPES
            .register("components", () -> new FluidIngredientType<>(ExDataComponentFluidIngredient.CODEC));
}
