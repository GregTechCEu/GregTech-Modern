package com.gregtechceu.gtceu.data.recipe.builder;

import com.gregtechceu.gtceu.api.recipe.ShapedFluidContainerRecipe;

import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ShapedFluidContainerRecipeBuilder extends ShapedRecipeBuilder {

    public ShapedFluidContainerRecipeBuilder(@Nullable Identifier id) {
        super(id);
    }

    public void save(RecipeOutput consumer) {
        var recipeId = id == null ? defaultId() : id;
        ShapedRecipe recipe = new ShapedFluidContainerRecipe(
                Objects.requireNonNullElse(this.group, ""),
                RecipeBuilder.determineCraftingBookCategory(this.category),
                ShapedRecipePattern.of(key, rows),
                this.output, false);
        consumer.accept(RecipeBuilderUtil.recipeKey(recipeId.withPrefix("shaped/")), recipe, null);
    }
}
