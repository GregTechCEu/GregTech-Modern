package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.core.mixins.ShapedRecipeAccessor;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

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

    public StrictShapedRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern,
                              ItemStack result, boolean showNotification) {
        super(group, category, pattern, result, showNotification);
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
                Ingredient ingredient = Ingredient.EMPTY;
                if (k >= 0 && l >= 0 && k < this.getWidth() && l < this.getHeight()) {
                    ingredient = this.getIngredients().get(k + l * this.getWidth());
                }
                if (ingredient.test(craftingInventory.getItem(i + j * craftingInventory.width()))) continue;
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

        public static final MapCodec<StrictShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC)
                        .forGetter(ShapedRecipe::category),
                ShapedRecipePattern.MAP_CODEC.forGetter(val -> ((ShapedRecipeAccessor) val).getPattern()),
                ItemStack.CODEC.fieldOf("result").forGetter(val -> ((ShapedRecipeAccessor) val).getResult()),
                Codec.BOOL.optionalFieldOf("show_notification", true)
                        .forGetter(val -> ((ShapedRecipeAccessor) val).getShowNotification()))
                .apply(instance, StrictShapedRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, StrictShapedRecipe> STREAM_CODEC = StreamCodec
                .composite(
                        ByteBufCodecs.STRING_UTF8, ShapedRecipe::getGroup,
                        CraftingBookCategory.STREAM_CODEC, ShapedRecipe::category,
                        ShapedRecipePattern.STREAM_CODEC, val -> ((ShapedRecipeAccessor) val).getPattern(),
                        ItemStack.STREAM_CODEC, val -> ((ShapedRecipeAccessor) val).getResult(),
                        ByteBufCodecs.BOOL, ShapedRecipe::showNotification,
                        StrictShapedRecipe::new);

        @Override
        public MapCodec<StrictShapedRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, StrictShapedRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
