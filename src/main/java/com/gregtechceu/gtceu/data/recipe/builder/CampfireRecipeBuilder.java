package com.gregtechceu.gtceu.data.recipe.builder;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.utils.NBTToJsonConverter;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;

import com.google.gson.JsonObject;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author JuiceyBeans
 * @date 2024/10/12
 * @implNote CampfireRecipeBuilder
 */
@Accessors(chain = true, fluent = true)
public class CampfireRecipeBuilder {

    private Ingredient input;
    @Setter
    protected String group;
    @Setter
    protected CookingBookCategory category = CookingBookCategory.FOOD;

    private ItemStack output = ItemStack.EMPTY;
    @Setter
    private float experience;
    @Setter
    private int cookingTime;
    @Setter
    protected ResourceLocation id;

    public CampfireRecipeBuilder(@Nullable ResourceLocation id) {
        this.id = id;
    }

    public CampfireRecipeBuilder input(TagKey<Item> itemStack) {
        return input(Ingredient.of(itemStack));
    }

    public CampfireRecipeBuilder input(ItemStack itemStack) {
        if (!itemStack.getComponentsPatch().isEmpty()) {
            input = DataComponentIngredient.of(true, itemStack);
        } else {
            input = Ingredient.of(itemStack);
        }
        return this;
    }

    public CampfireRecipeBuilder input(ItemLike itemLike) {
        return input(Ingredient.of(itemLike));
    }

    public CampfireRecipeBuilder input(Ingredient ingredient) {
        input = ingredient;
        return this;
    }

    public CampfireRecipeBuilder output(ItemStack itemStack) {
        this.output = itemStack.copy();
        return this;
    }

    public CampfireRecipeBuilder output(ItemStack itemStack, int count) {
        this.output = itemStack.copy();
        this.output.setCount(count);
        return this;
    }

    protected ResourceLocation defaultId() {
        return BuiltInRegistries.ITEM.getKey(output.getItem());
    }

    private CampfireCookingRecipe create() {
        return new CampfireCookingRecipe(Objects.requireNonNullElse(this.group, ""), this.category, this.input, this.output,
                this.experience, this.cookingTime);
    }

    public void save(RecipeOutput consumer) {
        var recipeId = id == null ? defaultId() : id;
        consumer.accept(ResourceLocation.fromNamespaceAndPath(recipeId.getNamespace(), "campfire/" + recipeId.getPath()),
                create(), null);
    }

}
