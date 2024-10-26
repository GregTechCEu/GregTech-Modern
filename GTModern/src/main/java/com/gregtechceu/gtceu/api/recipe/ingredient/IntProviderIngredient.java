package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Stream;

public class IntProviderIngredient extends Ingredient {

    public static final ResourceLocation TYPE = GTCEu.id("int_provider");

    @Getter
    protected final IntProvider countProvider;
    @Setter
    protected Integer sampledCount = null;
    @Getter
    protected final Ingredient inner;
    @Setter
    protected ItemStack[] itemStacks = null;

    public IntProviderIngredient(Ingredient inner, IntProvider countProvider) {
        super(Stream.empty());
        this.inner = inner;
        this.countProvider = countProvider;
    }

    public IntProviderIngredient(@NotNull TagKey<Item> tag, IntProvider amount) {
        this(Ingredient.of(tag), amount);
    }

    public static IntProviderIngredient create(Ingredient inner, IntProvider countProvider) {
        return new IntProviderIngredient(inner, countProvider);
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        return inner.test(stack);
    }

    @Override
    public ItemStack @NotNull [] getItems() {
        if (itemStacks == null)
            itemStacks = Arrays.stream(inner.getItems())
                    .map(i -> i.copyWithCount(getSampledCount(GTValues.RNG)))
                    .toArray(ItemStack[]::new);
        return itemStacks;
    }

    public int getSampledCount(@NotNull RandomSource random) {
        if (sampledCount == null) {
            sampledCount = countProvider.sample(random);
        }
        return sampledCount;
    }

    @Override
    public @NotNull IntList getStackingIds() {
        return inner.getStackingIds();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    @NotNull
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return SERIALIZER;
    }

    public static IntProviderIngredient fromJson(JsonObject json) {
        return SERIALIZER.parse(json);
    }

    @Override
    public @NotNull JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", TYPE.toString());
        json.add("count_provider", IntProvider.CODEC.encodeStart(JsonOps.INSTANCE, countProvider)
                .getOrThrow(false, GTCEu.LOGGER::error));
        json.add("ingredient", inner.toJson());
        return json;
    }

    public static final IIngredientSerializer<IntProviderIngredient> SERIALIZER = new IIngredientSerializer<>() {

        @Override
        public @NotNull IntProviderIngredient parse(FriendlyByteBuf buffer) {
            IntProvider amount = IntProvider.CODEC.parse(NbtOps.INSTANCE, buffer.readNbt().get("provider"))
                    .getOrThrow(false, GTCEu.LOGGER::error);
            return new IntProviderIngredient(Ingredient.fromNetwork(buffer), amount);
        }

        @Override
        public @NotNull IntProviderIngredient parse(JsonObject json) {
            IntProvider amount = IntProvider.CODEC.parse(JsonOps.INSTANCE, json.get("count_provider"))
                    .getOrThrow(false, GTCEu.LOGGER::error);
            Ingredient inner = Ingredient.fromJson(json.get("ingredient"));
            return new IntProviderIngredient(inner, amount);
        }

        @Override
        public void write(FriendlyByteBuf buffer, IntProviderIngredient ingredient) {
            CompoundTag wrapper = new CompoundTag();
            wrapper.put("provider", IntProvider.CODEC.encodeStart(NbtOps.INSTANCE, ingredient.countProvider)
                    .getOrThrow(false, GTCEu.LOGGER::error));
            buffer.writeNbt(wrapper);
            ingredient.inner.toNetwork(buffer);
        }
    };
}
