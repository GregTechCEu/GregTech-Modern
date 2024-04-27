package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class GTIngredientTypes {
    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, GTCEu.MOD_ID);

    public static final DeferredHolder<IngredientType<?>, IngredientType<IntCircuitIngredient>> INT_CIRCUIT_INGREDIENT = INGREDIENT_TYPES.register("circuit", () -> new IngredientType<>(IntCircuitIngredient.CODEC));
    public static final DeferredHolder<IngredientType<?>, IngredientType<SizedIngredient>> SIZED_INGREDIENT = INGREDIENT_TYPES.register("sized", () -> new IngredientType<>(SizedIngredient.CODEC));

}
