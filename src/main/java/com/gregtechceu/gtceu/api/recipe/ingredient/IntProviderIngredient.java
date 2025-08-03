package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class IntProviderIngredient extends Ingredient {

    public static final ResourceLocation TYPE = GTCEu.id("int_provider");
    public static final ItemStack[] EMPTY_STACK_ARRAY = new ItemStack[0];

    @Getter
    protected final IntProvider countProvider;
    @Setter
    protected int sampledCount = -1;
    @Getter
    protected final Ingredient inner;
    @Setter
    protected ItemStack[] itemStacks = null;

    protected IntProviderIngredient(Ingredient inner, IntProvider countProvider) {
        super(Stream.empty());
        this.inner = inner;
        this.countProvider = countProvider;
    }

    public static IntProviderIngredient of(Ingredient inner, IntProvider countProvider) {
        Preconditions.checkArgument(countProvider.getMinValue() >= 0,
                "IntProviderIngredient must have a min value of at least 0.");
        return new IntProviderIngredient(inner, countProvider);
    }

    public static IntProviderIngredient of(ItemStack stack, IntProvider countProvider) {
        Ingredient inner = stack.hasTag() ? StrictNBTIngredient.of(stack) : Ingredient.of(stack);
        return of(inner, countProvider);
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        return inner.test(stack);
    }

    @Override
    public ItemStack @NotNull [] getItems() {
        if (itemStacks == null) {
            int cachedCount = getSampledCount(GTValues.RNG);
            if (cachedCount == 0) {
                return EMPTY_STACK_ARRAY;
            }
            var innerStacks = inner.getItems();
            this.itemStacks = new ItemStack[innerStacks.length];
            for (int i = 0; i < itemStacks.length; i++) {
                itemStacks[i] = innerStacks[i].copyWithCount(cachedCount);
            }
        }
        return itemStacks;
    }

    public @NotNull ItemStack getMaxSizeStack() {
        if (inner.getItems().length == 0) return ItemStack.EMPTY;
        else return inner.getItems()[0].copyWithCount(countProvider.getMaxValue());
    }

    public int getSampledCount(@NotNull RandomSource random) {
        if (sampledCount == -1) {
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
