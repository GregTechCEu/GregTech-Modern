package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.*;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.mojang.serialization.MapCodec;

public class GTRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister
            .create(Registries.RECIPE_SERIALIZER, GTCEu.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<StrictShapedRecipe>> CRAFTING_SHAPED_STRICT = RECIPE_SERIALIZERS
            .register("crafting_shaped_strict",
                    () -> new RecipeSerializer<>(StrictShapedRecipe.Serializer.CODEC,
                            StrictShapedRecipe.Serializer.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapedEnergyTransferRecipe>> CRAFTING_SHAPED_ENERGY_TRANSFER = RECIPE_SERIALIZERS
            .register("crafting_shaped_energy_transfer",
                    () -> new RecipeSerializer<>(ShapedEnergyTransferRecipe.Serializer.CODEC,
                            ShapedEnergyTransferRecipe.Serializer.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapedFluidContainerRecipe>> CRAFTING_SHAPED_FLUID_CONTAINER = RECIPE_SERIALIZERS
            .register("crafting_shaped_fluid_container",
                    () -> new RecipeSerializer<>(ShapedFluidContainerRecipe.Serializer.CODEC,
                            ShapedFluidContainerRecipe.Serializer.STREAM_CODEC));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ToolHeadReplaceRecipe>> CRAFTING_TOOL_HEAD_REPLACE = RECIPE_SERIALIZERS
            .register("crafting_tool_head_replace",
                    () -> new RecipeSerializer<>(MapCodec.unit(ToolHeadReplaceRecipe::new),
                            StreamCodec.<RegistryFriendlyByteBuf, ToolHeadReplaceRecipe>unit(
                                    new ToolHeadReplaceRecipe())));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FacadeCoverRecipe>> CRAFTING_FACADE_COVER = RECIPE_SERIALIZERS
            .register("crafting_facade_cover",
                    () -> new RecipeSerializer<>(MapCodec.unit(FacadeCoverRecipe::new),
                            StreamCodec.<RegistryFriendlyByteBuf, FacadeCoverRecipe>unit(new FacadeCoverRecipe())));
}
