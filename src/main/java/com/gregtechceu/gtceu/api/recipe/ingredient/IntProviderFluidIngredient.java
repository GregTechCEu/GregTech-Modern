package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import com.google.common.base.Preconditions;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class IntProviderFluidIngredient extends FluidIngredient {

    public static final ResourceLocation TYPE = GTCEu.id("int_provider");

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
        IntProviderFluidIngredient ipfi = new IntProviderFluidIngredient(this, this.countProvider);
        ipfi.setSampledCount(this.sampledCount);
        return ipfi;
    }

    public IntProviderFluidIngredient replicate() {
        return new IntProviderFluidIngredient(this, this.countProvider);
    }

    @Override
    public int getAmount() {
        return -1;
    }

    @Override
    public FluidStack[] getStacks() {
        if (fluidStacks == null) {
            inner.setAmount(getSampledCount(GTValues.RNG));
            fluidStacks = inner.getStacks();
        }
        return fluidStacks;
    }

    public @NotNull FluidStack getMaxSizeStack(){
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
    public boolean isEmpty() { return inner.isEmpty(); }

    public static IntProviderFluidIngredient of(FluidIngredient inner, IntProvider provider) {
        return new IntProviderFluidIngredient(inner.copy(), provider);
    }


}