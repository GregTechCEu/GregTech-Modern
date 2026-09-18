package com.gregtechceu.gtceu.data.recipe.builder;

import com.gregtechceu.gtceu.api.recipe.ShapedFluidContainerRecipe;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ShapedFluidContainerRecipeBuilder extends ShapedRecipeBuilder {

    public ShapedFluidContainerRecipeBuilder(@Nullable Identifier id) {
        super(id);
    }

    public void save(Consumer<FinishedRecipe> consumer) {
        consumer.accept(new FinishedRecipe() {

            @Override
            public void serializeRecipeData(JsonObject pJson) {
                toJson(pJson);
            }

            @Override
            public Identifier getId() {
                var ID = id == null ? defaultId() : id;
                return ID.withPath("shaped_fluid_container/" + ID.getPath());
            }

            @Override
            public RecipeSerializer<?> getType() {
                return ShapedFluidContainerRecipe.SERIALIZER;
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public Identifier getAdvancementId() {
                return null;
            }
        });
    }
}
