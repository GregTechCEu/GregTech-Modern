package com.gregtechceu.gtceu.integration.kjs.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import dev.latvian.mods.kubejs.recipe.schema.RecipeNamespace;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaType;

import java.util.Optional;

public class WrappingRecipeSchemaType extends RecipeSchemaType {

    public WrappingRecipeSchemaType(RecipeNamespace namespace, ResourceLocation id, RecipeSchema schema,
                                    RecipeSerializer<?> serializer) {
        super(namespace, id, schema);
        this.serializer = Optional.of(serializer);
    }
}
