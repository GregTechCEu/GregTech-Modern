package com.gregtechceu.gtceu.api.recipe.content;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import com.mojang.serialization.Codec;

import java.util.stream.Stream;

public class SerializerIngredient implements IContentSerializer<SizedIngredient> {

    public static final Ingredient EMPTY_INGREDIENT = new Ingredient(EmptyIngredient.INSTANCE);
    public static final SizedIngredient EMPTY = new SizedIngredient(EMPTY_INGREDIENT, 1);
    public static SerializerIngredient INSTANCE = new SerializerIngredient();

    private SerializerIngredient() {}

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buf, SizedIngredient content) {
        SizedIngredient.STREAM_CODEC.encode(buf, content);
    }

    @Override
    public SizedIngredient fromNetwork(RegistryFriendlyByteBuf buf) {
        return SizedIngredient.STREAM_CODEC.decode(buf);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public SizedIngredient of(Object o) {
        if (o instanceof SizedIngredient ingredient) {
            return ingredient;
        } else if (o instanceof ItemStack itemStack) {
            return new SizedIngredient(Ingredient.of(itemStack.getItem()), itemStack.getCount());
        } else if (o instanceof ItemLike itemLike) {
            return SizedIngredient.of(itemLike, 1);
        } else if (o instanceof TagKey tag) {
            return new SizedIngredient(Ingredient.of(BuiltInRegistries.ITEM.getOrThrow((TagKey<Item>) tag)), 1);
        }
        throw new IllegalArgumentException("Unsupported item ingredient content: " + o);
    }

    @Override
    public SizedIngredient defaultValue() {
        return EMPTY;
    }

    @Override
    public Class<SizedIngredient> contentClass() {
        return SizedIngredient.class;
    }

    @Override
    public Codec<SizedIngredient> codec() {
        return SizedIngredient.NESTED_CODEC;
    }

    private enum EmptyIngredient implements ICustomIngredient {

        INSTANCE;

        @Override
        public boolean test(ItemStack stack) {
            return false;
        }

        @Override
        public Stream<Holder<Item>> items() {
            return Stream.empty();
        }

        @Override
        public boolean isSimple() {
            return true;
        }

        @Override
        public IngredientType<?> getType() {
            throw new UnsupportedOperationException("GTCEu's empty item ingredient sentinel is not serializable");
        }

        @Override
        public SlotDisplay display() {
            return SlotDisplay.Empty.INSTANCE;
        }
    }
}
