package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidContainerIngredient;
import com.gregtechceu.gtceu.common.data.GTRecipeSerializers;
import com.gregtechceu.gtceu.core.mixins.ShapedRecipeAccessor;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

// TODO shapeless fluid container recipes
@NotNullByDefault
public class ShapedFluidContainerRecipe extends ShapedRecipe {

    public ShapedFluidContainerRecipe(String group, CraftingBookCategory category,
                                      ShapedRecipePattern pattern, ItemStack result,
                                      boolean showNotification) {
        this(new Recipe.CommonInfo(showNotification), new CraftingRecipe.CraftingBookInfo(category, group), pattern,
                ItemStackTemplate.fromNonEmptyStack(result));
    }

    private ShapedFluidContainerRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                                       ShapedRecipePattern pattern, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        NonNullList<ItemStack> items = NonNullList.withSize(inv.size(), ItemStack.EMPTY);

        // figure out all the fluid container ingredients' remainders.
        int replacedSlot = -1;
        OUTER_LOOP:
        for (int x = 0; x <= inv.width() - this.getWidth(); ++x) {
            for (int y = 0; y <= inv.height() - this.getHeight(); ++y) {
                var stack = this.findFluidReplacement(inv, x, y, false);
                if (stack != null) {
                    replacedSlot = stack.firstInt();
                    items.set(replacedSlot, stack.second());
                    break OUTER_LOOP;
                }

                stack = this.findFluidReplacement(inv, x, y, true);
                if (stack != null) {
                    replacedSlot = stack.firstInt();
                    items.set(replacedSlot, stack.second());
                    break OUTER_LOOP;
                }
            }
        }

        for (int i = 0; i < items.size(); ++i) {
            if (i == replacedSlot) {
                continue;
            }
            ItemStack item = inv.getItem(i);
        }

        return items;
    }

    /**
     * Checks if the region of a crafting inventory is match for the recipe.
     */
    @Nullable
    private IntObjectPair<ItemStack> findFluidReplacement(CraftingInput inv, int width, int height, boolean mirrored) {
        for (int x = 0; x < inv.width(); ++x) {
            for (int y = 0; y < inv.height(); ++y) {
                int offsetX = x - width;
                int offsetY = y - height;
                Ingredient ingredient = null;
                if (offsetX >= 0 && offsetY >= 0 && offsetX < this.getWidth() && offsetY < this.getHeight()) {
                    if (mirrored) {
                        ingredient = this.getIngredients()
                                .get(this.getWidth() - offsetX - 1 + offsetY * this.getWidth())
                                .orElse(null);
                    } else {
                        ingredient = this.getIngredients().get(offsetX + offsetY * this.getWidth())
                                .orElse(null);
                    }
                }

                if (ingredient != null &&
                        ingredient.getCustomIngredient() instanceof FluidContainerIngredient fluidContainerIngredient) {
                    int slot = x + y * inv.width();
                    ItemStack stack = inv.getItem(slot);
                    if (fluidContainerIngredient.test(stack)) {
                        return IntObjectPair.of(slot, fluidContainerIngredient.getExtractedStack(stack));
                    }
                }
            }
        }

        return null;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer) GTRecipeSerializers.CRAFTING_SHAPED_FLUID_CONTAINER.get();
    }

    public static class Serializer {

        // spotless:off
        public static final MapCodec<ShapedFluidContainerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::group),
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapedRecipe::category),
                ShapedRecipePattern.MAP_CODEC.forGetter(i -> i.pattern),
                ItemStack.CODEC.fieldOf("result").forGetter(val -> ((ShapedRecipeAccessor) val).getResult().create()),
                Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(ShapedRecipe::showNotification)
        ).apply(instance, ShapedFluidContainerRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ShapedFluidContainerRecipe> STREAM_CODEC = StreamCodec.of(
                ShapedFluidContainerRecipe.Serializer::toNetwork, ShapedFluidContainerRecipe.Serializer::fromNetwork
        );
        // spotless:on

        private static ShapedFluidContainerRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            ShapedRecipePattern pattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);
            ItemStack output = ItemStack.STREAM_CODEC.decode(buffer);
            boolean showNotification = buffer.readBoolean();
            return new ShapedFluidContainerRecipe(group, category, pattern, output, showNotification);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, ShapedFluidContainerRecipe recipe) {
            buffer.writeUtf(recipe.group());
            buffer.writeEnum(recipe.category());
            ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
            ItemStack.STREAM_CODEC.encode(buffer, ((ShapedRecipeAccessor) recipe).getResult().create());
            buffer.writeBoolean(recipe.showNotification());
        }
    }
}
