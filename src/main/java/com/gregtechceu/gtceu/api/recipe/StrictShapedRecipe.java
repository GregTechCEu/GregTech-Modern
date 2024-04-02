package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.core.mixins.ShapedRecipeAccessor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/7/24
 * @implNote StrictShapedRecipe
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StrictShapedRecipe extends ShapedRecipe {

    public static final RecipeSerializer<StrictShapedRecipe> SERIALIZER = new Serializer();

    public StrictShapedRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification) {
        super(group, category, pattern, result, showNotification);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        for (int i = 0; i <= inv.getWidth() - this.getWidth(); ++i) {
            for (int j = 0; j <= inv.getHeight() - this.getHeight(); ++j) {
                if (this.matches(inv, i, j)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the region of a crafting inventory is match for the recipe.
     */
    private boolean matches(CraftingContainer craftingInventory, int width, int height) {
        for (int i = 0; i < craftingInventory.getWidth(); ++i) {
            for (int j = 0; j < craftingInventory.getHeight(); ++j) {
                int k = i - width;
                int l = j - height;
                Ingredient ingredient = Ingredient.EMPTY;
                if (k >= 0 && l >= 0 && k < this.getWidth() && l < this.getHeight()) {
                    ingredient = this.getIngredients().get(k + l * this.getWidth());
                }
                if (ingredient.test(craftingInventory.getItem(i + j * craftingInventory.getWidth()))) continue;
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static class Serializer implements RecipeSerializer<StrictShapedRecipe> {
        public static final Codec<StrictShapedRecipe> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(ShapedRecipe::getGroup),
                    CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapedRecipe::category),
                    ShapedRecipePattern.MAP_CODEC.forGetter(val -> ((ShapedRecipeAccessor)val).getPattern()),
                    ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter(val -> ((ShapedRecipeAccessor)val).getResult()),
                    ExtraCodecs.strictOptionalField(Codec.BOOL, "show_notification", true).forGetter(val -> ((ShapedRecipeAccessor)val).getShowNotification())
                )
                .apply(instance, StrictShapedRecipe::new)
        );

        @Override
        public StrictShapedRecipe fromNetwork(FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            ShapedRecipePattern pattern = ShapedRecipePattern.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            boolean showNotification = buffer.readBoolean();
            return new StrictShapedRecipe(group, category, pattern, result, showNotification);
        }

        @Override
        public Codec<StrictShapedRecipe> codec() {
            return CODEC;
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, StrictShapedRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            ((ShapedRecipeAccessor)recipe).getPattern().toNetwork(buffer);
            buffer.writeItem(((ShapedRecipeAccessor)recipe).getResult());
            buffer.writeBoolean(((ShapedRecipeAccessor) recipe).getShowNotification());
        }
    }
}
