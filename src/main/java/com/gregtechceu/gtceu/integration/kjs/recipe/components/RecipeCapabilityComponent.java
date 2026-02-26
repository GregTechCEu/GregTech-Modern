package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

public class RecipeCapabilityComponent implements RecipeComponent<RecipeCapability<?>> {

    // spotless:off
    public static final RecipeComponentType<RecipeCapability<?>> RECIPE_CAPABILITY = RecipeComponentType.unit(GTCEu.id("recipe_capability"), new RecipeCapabilityComponent());
    // spotless:on

    @Override
    public Codec<RecipeCapability<?>> codec() {
        return RecipeCapability.DIRECT_CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(RecipeCapability.class);
    }

    @Override
    public String toString() {
        return "recipe_capability";
    }

    @Override
    public RecipeComponentType<RecipeCapability<?>> type() {
        return RECIPE_CAPABILITY;
    }
}
