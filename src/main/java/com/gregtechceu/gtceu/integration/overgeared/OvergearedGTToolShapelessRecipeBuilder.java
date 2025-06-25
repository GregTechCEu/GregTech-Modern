package com.gregtechceu.gtceu.integration.overgeared;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.CraftingRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.data.recipes.RecipeBuilder.ROOT_RECIPE_ADVANCEMENT;

public class OvergearedGTToolShapelessRecipeBuilder extends CraftingRecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final List<Ingredient> ingredients = Lists.newArrayList();
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
    @Nullable
    private String group;

    public OvergearedGTToolShapelessRecipeBuilder(RecipeCategory category, ItemLike result, int count) {
        this.category = category;
        this.result = result.asItem();
        this.count = count;
    }

    public static OvergearedGTToolShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result) {
        return new OvergearedGTToolShapelessRecipeBuilder(category, result, 1);
    }

    public static OvergearedGTToolShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result, int count) {
        return new OvergearedGTToolShapelessRecipeBuilder(category, result, count);
    }

    public OvergearedGTToolShapelessRecipeBuilder requires(TagKey<Item> tag) {
        return this.requires(Ingredient.of(tag));
    }

    public OvergearedGTToolShapelessRecipeBuilder requires(ItemLike item) {
        return this.requires(item, 1);
    }

    public OvergearedGTToolShapelessRecipeBuilder requires(ItemLike item, int pQuantity) {
        for(int i = 0; i < pQuantity; ++i) {
            this.requires(Ingredient.of(item));
        }

        return this;
    }

    public OvergearedGTToolShapelessRecipeBuilder requires(Ingredient ingredient) {
        return this.requires(ingredient, 1);
    }

    public OvergearedGTToolShapelessRecipeBuilder requires(Ingredient ingredient, int pQuantity) {
        for(int i = 0; i < pQuantity; ++i) {
            this.ingredients.add(ingredient);
        }

        return this;
    }

    public OvergearedGTToolShapelessRecipeBuilder unlockedBy(String name, CriterionTriggerInstance trigger) {
        this.advancement.addCriterion(name, trigger);
        return this;
    }

    public OvergearedGTToolShapelessRecipeBuilder group(@Nullable String name) {
        this.group = name;
        return this;
    }

    public Item getResult() {
        return this.result;
    }

    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        this.ensureValid(id);
        this.advancement.parent(ROOT_RECIPE_ADVANCEMENT)
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(RequirementsStrategy.OR);
        consumer.accept(new OvergearedGTToolShapelessRecipeBuilder.Result(id, this.result, this.count,
                this.group == null ? "" : this.group, determineBookCategory(this.category), 
                this.ingredients, this.advancement, id.withPrefix("recipes/" +
                this.category.getFolderName() + "/")));
    }

    private void ensureValid(ResourceLocation id) {
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }

    public static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final Item result;
        private final int count;
        private final String group;
        private final CraftingBookCategory category;
        private final List<Ingredient> ingredients;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        public Result(ResourceLocation id, Item result, int count, String pGroup, CraftingBookCategory category,
                      List<Ingredient> ingredients, Advancement.Builder advancement,
                      ResourceLocation advancementId) {
            this.id = id;
            this.result = result;
            this.count = count;
            this.group = pGroup;
            this.category = category;
            this.ingredients = ingredients;
            this.advancement = advancement;
            this.advancementId = advancementId;
        }

        public void serializeRecipeData(JsonObject json) {
            if (!this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }

            json.addProperty("category", this.category.getSerializedName());
            JsonArray jsonArray = new JsonArray();

            for(Ingredient ingredient : this.ingredients) {
                jsonArray.add(ingredient.toJson());
            }

            json.add("ingredients", jsonArray);
            JsonObject jsonVoorhees = new JsonObject();
            jsonVoorhees.addProperty("item", BuiltInRegistries.ITEM.getKey(this.result).toString());
            if (this.count > 1) {
                jsonVoorhees.addProperty("count", this.count);
            }

            json.add("result", jsonVoorhees);
        }

        public RecipeSerializer<?> getType() {
            return OvergearedGTToolBonusRecipeBuilder.SERIALIZER;
        }

        @Override
        public @org.jetbrains.annotations.Nullable JsonObject serializeAdvancement() {
            return null;
        }

        @Override
        public @org.jetbrains.annotations.Nullable ResourceLocation getAdvancementId() {
            return null;
        }

        public ResourceLocation getId() {
            return this.id;
        }
    }
}