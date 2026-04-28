package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

public class ResourceLocationComponent implements RecipeComponent<Identifier> {

    // spotless:off
    public static final RecipeComponentType<Identifier> RESOURCE_LOCATION = RecipeComponentType.unit(ResourceLocation.fromIdentifier(GTCEu.id("tag")), new ResourceLocationComponent());
    // spotless:on

    @Override
    public Codec<Identifier> codec() {
        return Identifier.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(Identifier.class).or(TypeInfo.STRING);
    }

    @Override
    public String toString() {
        return "resource_location";
    }

    @Override
    public RecipeComponentType<Identifier> type() {
        return RESOURCE_LOCATION;
    }
}
