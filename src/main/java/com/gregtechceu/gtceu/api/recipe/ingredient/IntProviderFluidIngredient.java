package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraftforge.fluids.FluidStack;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

public class IntProviderFluidIngredient extends FluidIngredient {

    public static final Codec<IntProviderFluidIngredient> CODEC = ExtraCodecs.JSON
            .xmap(IntProviderFluidIngredient::fromJson, IntProviderFluidIngredient::toJson);

    @Getter
    private final IntProvider countProvider;
    @Setter
    protected int sampledCount = -1;
    @Getter
    private final FluidIngredient inner;
    @Setter
    protected FluidStack[] fluidStacks = null;

    protected IntProviderFluidIngredient(FluidIngredient inner, IntProvider provider) {
        super(inner.values, provider.getMaxValue(), null);
        this.inner = inner;
        this.countProvider = provider;
    }

    @Override
    public IntProviderFluidIngredient copy() {
        IntProviderFluidIngredient ipfi = new IntProviderFluidIngredient(this.inner, this.countProvider);
        ipfi.setSampledCount(this.sampledCount);
        return ipfi;
    }

    @Override
    public int getAmount() {
        return -1;
    }

    @Override
    public FluidStack[] getStacks() {
        if (fluidStacks == null) {
            int cachedAmount = getSampledCount(GTValues.RNG);
            if (cachedAmount == 0) {
                return EMPTY_STACK_ARRAY;
            }
            var innerStacks = inner.getStacks();
            this.fluidStacks = new FluidStack[innerStacks.length];
            for (int i = 0; i < fluidStacks.length; i++) {
                fluidStacks[i] = innerStacks[i].copy();
                fluidStacks[i].setAmount(cachedAmount);
            }
        }
        return fluidStacks;
    }

    public @NotNull FluidStack getMaxSizeStack() {
        FluidStack[] in = inner.getStacks();
        if (in.length == 0) return FluidStack.EMPTY;
        return new FluidStack(in[0], countProvider.getMaxValue());
    }

    public int getSampledCount(@NotNull RandomSource random) {
        if (sampledCount == -1) {
            sampledCount = countProvider.sample(random);
        }
        return sampledCount;
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    public static IntProviderFluidIngredient of(FluidIngredient inner, IntProvider provider) {
        return new IntProviderFluidIngredient(inner, provider);
    }

    public static IntProviderFluidIngredient of(FluidStack inner, int min, int max) {
        return IntProviderFluidIngredient.of(FluidIngredient.of(inner), UniformInt.of(min, max));
    }

    @Override
    public @NotNull JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.add("count_provider", IntProvider.CODEC.encodeStart(JsonOps.INSTANCE, countProvider)
                .getOrThrow(false, GTCEu.LOGGER::error));
        json.add("inner", inner.toJson());
        return json;
    }

    public static IntProviderFluidIngredient fromJson(JsonElement json) {
        if (json == null || json.isJsonNull()) {
            throw new JsonSyntaxException("Fluid ingredient cannot be null");
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(json, "ingredient");
        IntProvider amount = IntProvider.CODEC.parse(JsonOps.INSTANCE, jsonObject.get("count_provider"))
                .getOrThrow(false, GTCEu.LOGGER::error);
        FluidIngredient inner = FluidIngredient.fromJson(jsonObject.get("inner"));
        return new IntProviderFluidIngredient(inner, amount);
    }

    public CompoundTag toNBT() {
        return (CompoundTag) JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, this.toJson());
    }

    public static IntProviderFluidIngredient fromNBT(CompoundTag nbt) {
        return IntProviderFluidIngredient.fromJson(NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, nbt));
    }
}
