package com.gregtechceu.gtceu.integration.overgeared;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.recipe.ForgingQualityShapelessRecipe;

import java.util.Random;

/**
 * Overgeared's shapeless recipe builder for tools doesn't work with GT tools,
 * Hence the existence of this class
 */
public class OvergearedGTToolBonusRecipeBuilder extends ForgingQualityShapelessRecipe {
    public static final OvergearedGTToolBonusRecipeBuilder.Serializer SERIALIZER = new OvergearedGTToolBonusRecipeBuilder.Serializer();

    public OvergearedGTToolBonusRecipeBuilder(ResourceLocation id, String group, CraftingBookCategory category,
                                              ItemStack result, NonNullList<Ingredient> ingredients) {
        super(id, group, category, result, ingredients);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack result = super.assemble(container, registryAccess);

        if (!ServerConfig.ENABLE_MINIGAME.get()) {
            // When minigame is disabled
            boolean hasUnpolishedQualityItem = false;

            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack ingredient = container.getItem(i);
                if (ingredient.hasTag()) {
                    CompoundTag tag = ingredient.getTag();
                    // Check if item has quality but isn't polished
                    if (!tag.contains("Polished") || !tag.getBoolean("Polished")) {
                        hasUnpolishedQualityItem = true;
                        break; // No need to check further if we found one
                    }
                }
            }

            // Prevent crafting if any unpolished quality items exist
            if (hasUnpolishedQualityItem) {
                return ItemStack.EMPTY;
            }

            // Remove any ForgingQuality tag from result if present
            if (result.hasTag() && result.getTag().contains("ForgingQuality")) {
                result.getTag().remove("ForgingQuality");
                if (result.getTag().isEmpty()) {
                    result.setTag(null); // Remove empty tag
                }
            }

            return result;
        }

        // Original minigame-enabled logic
        CompoundTag resultTag = result.getOrCreateTag();
        String foundQuality = null;
        boolean isPolished = false;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack ingredient = container.getItem(i);
            if (ingredient.hasTag()) {
                CompoundTag tag = ingredient.getTag();
                if (tag.contains("ForgingQuality")) {
                    foundQuality = tag.getString("ForgingQuality");
                }
                if (tag.contains("Polished") && tag.getBoolean("Polished")) {
                    isPolished = true;
                }
            }
        }

        if (foundQuality != null) {
            String resultQuality = foundQuality;
            if (!isPolished) {
                resultQuality = switch (foundQuality) {
                    case "perfect" -> "expert";
                    case "expert" -> "well";
                    case "well" -> "poor";
                    default -> foundQuality;
                };
            }
            resultTag.putString("ForgingQuality", resultQuality);
            result.setTag(resultTag);

            var durability = result.getMaxDamage();
            var rand = new Random();
            // apply bonus durability of 1-10% of max durability
            ToolHelper.getToolTag(result).putInt(ToolHelper.MAX_DURABILITY_KEY, durability * (1 + rand.nextInt(1, 11)));
        }

        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return OvergearedGTToolBonusRecipeBuilder.SERIALIZER;
    }

    public OvergearedGTToolBonusRecipeBuilder fromJson(ResourceLocation recipeId, JsonObject json) {
        ShapelessRecipe baseRecipe = ShapelessRecipe.Serializer.SHAPELESS_RECIPE.fromJson(recipeId, json);
        return new OvergearedGTToolBonusRecipeBuilder(recipeId, baseRecipe.getGroup(), baseRecipe.category(),
                baseRecipe.getResultItem(null), baseRecipe.getIngredients());
    }

    public OvergearedGTToolBonusRecipeBuilder fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        ShapelessRecipe baseRecipe = ShapelessRecipe.Serializer.SHAPELESS_RECIPE.fromNetwork(recipeId, buffer);
        return new OvergearedGTToolBonusRecipeBuilder(recipeId, baseRecipe.getGroup(), baseRecipe.category(),
                baseRecipe.getResultItem(null), baseRecipe.getIngredients());
    }

    public void toNetwork(FriendlyByteBuf buffer, OvergearedGTToolBonusRecipeBuilder recipe) {
        net.minecraft.world.item.crafting.ShapelessRecipe.Serializer.SHAPELESS_RECIPE.toNetwork(buffer, recipe);
    }
}
