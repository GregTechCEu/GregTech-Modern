package com.gregtechceu.gtceu.data.tag;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;

import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class GTIngredientTypes {

    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.INGREDIENT_TYPES, GTCEu.MOD_ID);

    public static final DeferredHolder<IngredientType<?>, IngredientType<IntCircuitIngredient>> INT_CIRCUIT_INGREDIENT = INGREDIENT_TYPES
            .register("circuit", () -> new IngredientType<>(IntCircuitIngredient.CODEC));

    public static final DeferredHolder<IngredientType<?>, IngredientType<IntProviderIngredient>> INT_PROVIDER_INGREDIENT = INGREDIENT_TYPES
            .register("int_provider", () -> new IngredientType<>(IntProviderIngredient.CODEC));
}
