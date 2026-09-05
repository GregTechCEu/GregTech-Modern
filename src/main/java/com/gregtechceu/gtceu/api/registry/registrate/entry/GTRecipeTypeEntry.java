package com.gregtechceu.gtceu.api.registry.registrate.entry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import org.jetbrains.annotations.ApiStatus;

public class GTRecipeTypeEntry extends RegistryEntry<RecipeType<?>, GTRecipeType> {

    public GTRecipeTypeEntry(AbstractRegistrate<?> owner, DeferredHolder<RecipeType<?>, GTRecipeType> key) {
        super(owner, key);
    }

    public GTRecipeBuilder recipeBuilder(ResourceLocation id) {
        return value().recipeBuilder(id);
    }

    public GTRecipeBuilder recipeBuilder(ResourceLocation id, Object... append) {
        return value().recipeBuilder(id, append);
    }

    @ApiStatus.Internal
    public GTRecipeBuilder recipeBuilder(String id) {
        return value().recipeBuilder(GTCEu.id(id));
    }

    @ApiStatus.Internal
    public GTRecipeBuilder recipeBuilder(String id, Object... append) {
        return value().recipeBuilder(GTCEu.id(id), append);
    }

    public GTRecipeCategory getCategory() {
        return value().getCategory();
    }

    public GTRecipeBuilder copyFrom(GTRecipeBuilder builder) {
        return value().copyFrom(builder);
    }

    public int getMaxInputs(RecipeCapability<?> cap) {
        return value().getMaxInputs(cap);
    }

    public int getMaxOutputs(RecipeCapability<?> cap) {
        return value().getMaxOutputs(cap);
    }

    public int getMaxSlots(RecipeCapability<?> cap, IO io) {
        return io == IO.IN ? getMaxInputs(cap) : getMaxOutputs(cap);
    }

    public Component getName() {
        return Component.translatable(getKey().location().toLanguageKey("recipe_type"));
    }

    public void addToMainCategory(GTRecipe recipe) {
        value().addToMainCategory(recipe);
    }
}
