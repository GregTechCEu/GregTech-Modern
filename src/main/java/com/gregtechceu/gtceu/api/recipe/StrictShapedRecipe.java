package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.common.data.GTRecipeSerializers;
import com.gregtechceu.gtceu.core.mixins.ShapedRecipeAccessor;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class StrictShapedRecipe extends ShapedRecipe {

    public StrictShapedRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern,
                              ItemStack result, boolean showNotification) {
        this(new Recipe.CommonInfo(showNotification), new CraftingRecipe.CraftingBookInfo(category, group), pattern,
                ItemStackTemplate.fromNonEmptyStack(result));
    }

    private StrictShapedRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                               ShapedRecipePattern pattern, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        for (int i = 0; i <= inv.width() - this.getWidth(); ++i) {
            for (int j = 0; j <= inv.height() - this.getHeight(); ++j) {
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
    private boolean matches(CraftingInput craftingInventory, int width, int height) {
        for (int i = 0; i < craftingInventory.width(); ++i) {
            for (int j = 0; j < craftingInventory.height(); ++j) {
                int k = i - width;
                int l = j - height;
                ItemStack stack = craftingInventory.getItem(i + j * craftingInventory.width());
                boolean matches = stack.isEmpty();
                if (k >= 0 && l >= 0 && k < this.getWidth() && l < this.getHeight()) {
                    matches = this.getIngredients().get(k + l * this.getWidth())
                            .map(ingredient -> ingredient.test(stack))
                            .orElse(stack.isEmpty());
                }
                if (matches) continue;
                return false;
            }
        }
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) GTRecipeSerializers.CRAFTING_SHAPED_STRICT.get();
    }

    public static class Serializer {

        public static final MapCodec<StrictShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::group),
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC)
                        .forGetter(ShapedRecipe::category),
                ShapedRecipePattern.MAP_CODEC.forGetter(val -> val.pattern),
                ItemStack.CODEC.fieldOf("result").forGetter(val -> ((ShapedRecipeAccessor) val).getResult().create()),
                Codec.BOOL.optionalFieldOf("show_notification", true)
                        .forGetter(ShapedRecipe::showNotification))
                .apply(instance, StrictShapedRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, StrictShapedRecipe> STREAM_CODEC = StreamCodec
                .composite(
                        ByteBufCodecs.STRING_UTF8, ShapedRecipe::group,
                        CraftingBookCategory.STREAM_CODEC, ShapedRecipe::category,
                        ShapedRecipePattern.STREAM_CODEC, val -> val.pattern,
                        ItemStack.STREAM_CODEC, val -> ((ShapedRecipeAccessor) val).getResult().create(),
                        ByteBufCodecs.BOOL, ShapedRecipe::showNotification,
                        StrictShapedRecipe::new);
    }
}
