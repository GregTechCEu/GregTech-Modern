package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.content.ContentListMap;
import lombok.Getter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class GTRecipeDefinition implements net.minecraft.world.item.crafting.Recipe<Container> {

    @Getter
    public final ResourceLocation id;

    public final GTRecipeType recipeType;
    public final GTRecipeCategory category;

    public final ContentListMap inputs;
    public final ContentListMap outputs;
    public final ContentListMap tickInputs;
    public final ContentListMap tickOutputs;

    public final int duration;

    public final List<RecipeCondition<?>> conditions;

    public final CompoundTag data;

    public final int tier;

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
                duration, category);
    }

}
