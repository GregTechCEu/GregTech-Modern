package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

public class ResourceLocationComponent implements RecipeComponent<ResourceLocation> {

    // spotless:off
    public static final RecipeComponentType<ResourceLocation> RESOURCE_LOCATION = RecipeComponentType.unit(GTCEu.id("tag"), new ResourceLocationComponent());
    // spotless:on

    @Override
    public Codec<ResourceLocation> codec() {
        return ResourceLocation.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(ResourceLocation.class).or(TypeInfo.STRING);
    }

    @Override
    public String toString() {
        return "resource_location";
    }

    @Override
    public RecipeComponentType<ResourceLocation> type() {
        return RESOURCE_LOCATION;
    }
}
