package com.gregtechceu.gtceu.data.recipe.builder;

import com.gregtechceu.gtceu.api.recipe.ShapedFluidContainerRecipe;

import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ShapedFluidContainerRecipeBuilder extends ShapedRecipeBuilder {

    public ShapedFluidContainerRecipeBuilder(@Nullable ResourceLocation id) {
        super(id);
    }

    public void save(RecipeOutput consumer) {
        var recipeId = id == null ? defaultId() : id;
        ShapedFluidContainerRecipe recipe = new ShapedFluidContainerRecipe(
                Objects.requireNonNullElse(this.group, ""),
                RecipeBuilder.determineBookCategory(this.category),
                ShapedRecipePattern.of(key, rows),
                this.output,
                false);
        consumer.accept(ResourceLocation.fromNamespaceAndPath(recipeId.getNamespace(), "shaped_fluid_container/" + recipeId.getPath()),
                recipe,
                null);
    }
}
