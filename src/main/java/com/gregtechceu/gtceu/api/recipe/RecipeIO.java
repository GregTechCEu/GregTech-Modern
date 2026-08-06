package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Map;

public record RecipeIO(
                       Map<RecipeCapability<?>, List<Content>> inputs,
                       Map<RecipeCapability<?>, List<Content>> outputs,
                       Map<RecipeCapability<?>, List<Content>> tickInputs,
                       Map<RecipeCapability<?>, List<Content>> tickOutputs,
                       Map<RecipeCapability<?>, ChanceLogic> inputChanceLogics,
                       Map<RecipeCapability<?>, ChanceLogic> outputChanceLogics,
                       Map<RecipeCapability<?>, ChanceLogic> tickInputChanceLogics,
                       Map<RecipeCapability<?>, ChanceLogic> tickOutputChanceLogics) {

    public static final Codec<RecipeIO> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RecipeCapability.CODEC.optionalFieldOf("inputs", Map.of()).forGetter(val -> val.inputs),
            RecipeCapability.CODEC.optionalFieldOf("outputs", Map.of()).forGetter(val -> val.outputs),
            RecipeCapability.CODEC.optionalFieldOf("tickInputs", Map.of()).forGetter(val -> val.tickInputs),
            RecipeCapability.CODEC.optionalFieldOf("tickOutputs", Map.of()).forGetter(val -> val.tickOutputs),
            Codec.unboundedMap(RecipeCapability.DIRECT_CODEC, GTRegistries.CHANCE_LOGICS.codec())
                    .optionalFieldOf("inputChanceLogics", Map.of()).forGetter(val -> val.inputChanceLogics),
            Codec.unboundedMap(RecipeCapability.DIRECT_CODEC, GTRegistries.CHANCE_LOGICS.codec())
                    .optionalFieldOf("outputChanceLogics", Map.of()).forGetter(val -> val.outputChanceLogics),
            Codec.unboundedMap(RecipeCapability.DIRECT_CODEC, GTRegistries.CHANCE_LOGICS.codec())
                    .optionalFieldOf("tickInputChanceLogics", Map.of()).forGetter(val -> val.tickInputChanceLogics),
            Codec.unboundedMap(RecipeCapability.DIRECT_CODEC, GTRegistries.CHANCE_LOGICS.codec())
                    .optionalFieldOf("tickOutputChanceLogics", Map.of()).forGetter(val -> val.tickOutputChanceLogics))
            .apply(instance, RecipeIO::new));
}
