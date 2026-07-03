package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.content.ContentListMap;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class GTRecipeDefinition implements net.minecraft.world.item.crafting.Recipe<Container> {

    @Getter
    public final ResourceLocation id;

    public final GTRecipeType recipeType;
    public final GTRecipeCategory category;

    public final int tier;

    public final ContentListMap inputs;
    public final ContentListMap outputs;
    public final ContentListMap tickInputs;
    public final ContentListMap tickOutputs;

    public final int duration;

    public final List<RecipeCondition<?>> conditions;

    public final CompoundTag data;

    public GTRecipeDefinition(ResourceLocation id, GTRecipeType recipeType, GTRecipeCategory category,
                              ContentListMap inputs, ContentListMap outputs,
                              ContentListMap tickInputs, ContentListMap tickOutputs,
                              int duration, List<RecipeCondition<?>> conditions, CompoundTag data, int tier) {
        this.id = id;
        this.recipeType = recipeType;
        this.category = category != GTRecipeCategory.DEFAULT ? category : recipeType.getCategory();
        this.inputs = inputs;
        this.outputs = outputs;
        this.tickInputs = tickInputs;
        this.tickOutputs = tickOutputs;
        this.duration = duration;
        this.conditions = conditions;
        this.data = data;
        this.tier = tier;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return GTRecipeSerializer.SERIALIZER;
    }

    @Override
    public GTRecipeType getType() {
        return recipeType;
    }

    public GTRecipe toRuntime() {
        return new GTRecipe(recipeType, id,
                inputs.copy(), outputs.copy(),
                tickInputs.copy(), tickOutputs.copy(),
                new ArrayList<>(conditions), data.copy(),
                tier, duration, category);
    }

    public GTRecipeDefinition withId(ResourceLocation id) {
        return new GTRecipeDefinition(id, recipeType, category,
                inputs.copy(), outputs.copy(),
                tickInputs.copy(), tickOutputs.copy(),
                duration, new ArrayList<>(conditions), data.copy(), tier);
    }

    public <T> List<T> getInputContents(RecipeCapability<T> capability) {
        return inputs.getOrDefault(capability, List.of());
    }

    public <T> List<T> getOutputContents(RecipeCapability<T> capability) {
        return outputs.getOrDefault(capability, List.of());
    }

    public <T> List<T> getTickInputContents(RecipeCapability<T> capability) {
        return tickInputs.getOrDefault(capability, List.of());
    }

    public <T> List<T> getTickOutputContents(RecipeCapability<T> capability) {
        return tickOutputs.getOrDefault(capability, List.of());
    }

    public List<List<AbstractMapIngredient>> getInputMapIngredients() {
        return buildMapIngredients(inputs);
    }

    public List<List<AbstractMapIngredient>> getTickInputMapIngredients() {
        return buildMapIngredients(tickInputs);
    }

    private static List<List<AbstractMapIngredient>> buildMapIngredients(ContentListMap contents) {
        List<List<AbstractMapIngredient>> ingredients = new ArrayList<>();
        contents.forEachEntry(new ContentListMap.EntryConsumer() {

            @Override
            public <T> void accept(RecipeCapability<T> capability, List<T> contents) {
                if (!capability.isRecipeSearchFilter()) return;
                for (var content : contents) {
                    List<AbstractMapIngredient> mapIngredients = new ArrayList<>(capability.getMapIngredients(content));
                    if (!mapIngredients.isEmpty()) {
                        ingredients.add(mapIngredients);
                    }
                }
            }
        });
        return ingredients;
    }
}
