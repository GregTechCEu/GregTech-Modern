package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidContainerIngredient;
import com.gregtechceu.gtceu.core.mixins.ShapedRecipeAccessor;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

// TODO shapeless fluid container recipes
public class ShapedFluidContainerRecipe extends ShapedRecipe {

    public static final RecipeSerializer<ShapedFluidContainerRecipe> SERIALIZER = new Serializer();

    public ShapedFluidContainerRecipe(ResourceLocation id, String group, CraftingBookCategory category,
                                      int width, int height,
                                      NonNullList<Ingredient> recipeItems, ItemStack result,
                                      boolean showNotification) {
        super(id, group, category, width, height, recipeItems, result, showNotification);
    }

    public ShapedFluidContainerRecipe(ResourceLocation id, String group, CraftingBookCategory category,
                                      int width, int height,
                                      NonNullList<Ingredient> recipeItems, ItemStack result) {
        this(id, group, category, width, height, recipeItems, result, true);
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingContainer inv) {
        NonNullList<ItemStack> items = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

        // figure out all the fluid container ingredients' remainders.
        int replacedSlot = -1;
        OUTER_LOOP:
        for (int x = 0; x <= inv.getWidth() - this.getWidth(); ++x) {
            for (int y = 0; y <= inv.getHeight() - this.getHeight(); ++y) {
                var stack = this.findFluidReplacement(inv, x, y, false);
                if (stack.firstInt() != -1) {
                    items.set(stack.firstInt(), stack.second());
                    replacedSlot = stack.firstInt();
                    break OUTER_LOOP;
                }

                stack = this.findFluidReplacement(inv, x, y, true);
                if (stack.firstInt() != -1) {
                    items.set(stack.firstInt(), stack.second());
                    replacedSlot = stack.firstInt();
                    break OUTER_LOOP;
                }
            }
        }

        for (int i = 0; i < items.size(); ++i) {
            if (i == replacedSlot) {
                continue;
            }
            ItemStack item = inv.getItem(i);
            if (item.hasCraftingRemainingItem()) {
                items.set(i, item.getCraftingRemainingItem());
            }
        }

        return items;
    }

    /**
     * Checks if the region of a crafting inventory is match for the recipe.
     */
    private IntObjectPair<ItemStack> findFluidReplacement(CraftingContainer inv, int width, int height,
                                                          boolean mirrored) {
        for (int x = 0; x < inv.getWidth(); ++x) {
            for (int y = 0; y < inv.getHeight(); ++y) {
                int offsetX = x - width;
                int offsetY = y - height;
                Ingredient ingredient = Ingredient.EMPTY;
                if (offsetX >= 0 && offsetY >= 0 && offsetX < this.getWidth() && offsetY < this.getHeight()) {
                    if (mirrored) {
                        ingredient = this.getIngredients()
                                .get(this.getWidth() - offsetX - 1 + offsetY * this.getWidth());
                    } else {
                        ingredient = this.getIngredients().get(offsetX + offsetY * this.getWidth());
                    }
                }

                if (ingredient instanceof FluidContainerIngredient fluidContainerIngredient) {
                    int slot = x + y * inv.getWidth();
                    ItemStack stack = inv.getItem(slot);
                    if (fluidContainerIngredient.test(stack)) {
                        return IntObjectPair.of(slot, fluidContainerIngredient.getExtractedStack(stack));
                    }
                }
            }
        }

        return IntObjectPair.of(-1, ItemStack.EMPTY);
    }

    public static class Serializer implements RecipeSerializer<ShapedFluidContainerRecipe> {

        @Override
        public ShapedFluidContainerRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            CraftingBookCategory category = CraftingBookCategory.CODEC
                    .byName(GsonHelper.getAsString(json, "category", null), CraftingBookCategory.MISC);
            Map<String, Ingredient> key = ShapedRecipeAccessor.callKeyFromJson(GsonHelper.getAsJsonObject(json, "key"));
            String[] pattern = ShapedRecipeAccessor
                    .callShrink(ShapedRecipeAccessor.callPatternFromJson(GsonHelper.getAsJsonArray(json, "pattern")));
            int xSize = pattern[0].length();
            int ySize = pattern.length;
            NonNullList<Ingredient> dissolved = ShapedRecipeAccessor.callDissolvePattern(pattern, key, xSize, ySize);
            ItemStack result = ShapedFluidContainerRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            boolean showNotification = GsonHelper.getAsBoolean(json, "show_notification", true);
            return new ShapedFluidContainerRecipe(recipeId, group, category,
                    xSize, ySize,
                    dissolved, result,
                    showNotification);
        }

        @Override
        public ShapedFluidContainerRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int xSize = buffer.readVarInt();
            int ySize = buffer.readVarInt();
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            String group = buffer.readUtf();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(xSize * ySize, Ingredient.EMPTY);
            ingredients.replaceAll($ -> Ingredient.fromNetwork(buffer));
            ItemStack result = buffer.readItem();
            boolean showNotification = buffer.readBoolean();
            return new ShapedFluidContainerRecipe(recipeId, group, category,
                    xSize, ySize,
                    ingredients, result,
                    showNotification);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ShapedFluidContainerRecipe recipe) {
            buffer.writeVarInt(recipe.getWidth());
            buffer.writeVarInt(recipe.getHeight());
            buffer.writeEnum(recipe.category());
            buffer.writeUtf(recipe.getGroup());
            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeItem(((ShapedRecipeAccessor) recipe).getResult());
            buffer.writeBoolean(recipe.showNotification());
        }
    }
}
