package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.kind.*;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GTRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister
            .create(Registries.RECIPE_SERIALIZER, GTCEu.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, StrictShapedRecipe.Serializer> CRAFTING_SHAPED_STRICT = RECIPE_SERIALIZERS
            .register("crafting_shaped_strict", StrictShapedRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, ShapedEnergyTransferRecipe.Serializer> CRAFTING_SHAPED_ENERGY_TRANSFER = RECIPE_SERIALIZERS
            .register("crafting_shaped_energy_transfer", ShapedEnergyTransferRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, ShapedFluidContainerRecipe.Serializer> CRAFTING_SHAPED_FLUID_CONTAINER = RECIPE_SERIALIZERS
            .register("crafting_shaped_fluid_container", ShapedFluidContainerRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ToolHeadReplaceRecipe>> CRAFTING_TOOL_HEAD_REPLACE = RECIPE_SERIALIZERS
            .register("crafting_tool_head_replace",
                    () -> new SimpleCraftingRecipeSerializer<>(ToolHeadReplaceRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<FacadeCoverRecipe>> CRAFTING_FACADE_COVER = RECIPE_SERIALIZERS
            .register("crafting_facade_cover",
                    () -> new SimpleCraftingRecipeSerializer<>(FacadeCoverRecipe::new));
}
