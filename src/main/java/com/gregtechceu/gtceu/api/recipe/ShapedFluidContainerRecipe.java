package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidContainerIngredient;
import com.gregtechceu.gtceu.core.mixins.ShapedRecipeAccessor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import com.mojang.datafixers.util.Pair;

// TODO shapeless fluid container recipes
public class ShapedFluidContainerRecipe extends ShapedRecipe {

    public static final RecipeSerializer<ShapedFluidContainerRecipe> SERIALIZER = new Serializer();

    public ShapedFluidContainerRecipe(String group, CraftingBookCategory category,
                                      ShapedRecipePattern pattern, ItemStack result,
                                      boolean showNotification) {
        super(group, category, pattern, result, showNotification);
    }

    public ShapedFluidContainerRecipe(String group, CraftingBookCategory category,
                                      ShapedRecipePattern pattern, ItemStack result) {
        this(group, category, pattern, result, true);
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
                if (stack.getFirst() != -1) {
                    items.set(stack.getFirst(), stack.getSecond());
                    replacedSlot = stack.getFirst();
                    break OUTER_LOOP;
                }

                stack = this.findFluidReplacement(inv, x, y, true);
                if (stack.getFirst() != -1) {
                    items.set(stack.getFirst(), stack.getSecond());
                    replacedSlot = stack.getFirst();
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
    private Pair<Integer, ItemStack> findFluidReplacement(CraftingInput inv, int width, int height,
                                                          boolean mirrored) {
        for (int x = 0; x < inv.width(); ++x) {
            for (int y = 0; y < inv.height(); ++y) {
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

                if (ingredient.getCustomIngredient() instanceof FluidContainerIngredient fluidContainerIngredient) {
                    int slot = x + y * inv.width();
                    ItemStack stack = inv.getItem(slot);
                    if (fluidContainerIngredient.test(stack)) {
                        return Pair.of(slot, fluidContainerIngredient.getExtractedStack(stack));
                    }
                }
            }
        }

        return Pair.of(-1, ItemStack.EMPTY);
    }

    public static class Serializer implements RecipeSerializer<ShapedFluidContainerRecipe> {

        public static final MapCodec<ShapedFluidContainerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapedRecipe::category),
                        ShapedRecipePattern.MAP_CODEC.forGetter(i -> i.pattern),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(val -> ((ShapedRecipeAccessor) val).getResult()),
                        Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(val -> ((ShapedRecipeAccessor) val).getShowNotification()))
                .apply(instance, ShapedFluidContainerRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapedFluidContainerRecipe> STREAM_CODEC = StreamCodec.of(
                ShapedFluidContainerRecipe.Serializer::toNetwork, ShapedFluidContainerRecipe.Serializer::fromNetwork
        );

        @Override
        public MapCodec<ShapedFluidContainerRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapedFluidContainerRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static ShapedFluidContainerRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            ShapedRecipePattern pattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);
            ItemStack output = ItemStack.STREAM_CODEC.decode(buffer);
            boolean showNotification = buffer.readBoolean();
            return new ShapedFluidContainerRecipe(group, category, pattern, output, showNotification);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, ShapedFluidContainerRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
            ItemStack.STREAM_CODEC.encode(buffer, ((ShapedRecipeAccessor)recipe).getResult());
            buffer.writeBoolean(((ShapedRecipeAccessor)recipe).getShowNotification());
        }
    }
}
