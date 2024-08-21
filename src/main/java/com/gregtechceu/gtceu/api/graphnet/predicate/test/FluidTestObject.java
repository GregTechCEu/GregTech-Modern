package com.gregtechceu.gtceu.api.graphnet.predicate.test;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

public final class FluidTestObject implements IPredicateTestObject, Predicate<FluidStack> {

    public final Fluid fluid;
    public final CompoundTag tag;

    private final int hash;

    public FluidTestObject(@NotNull FluidStack stack) {
        this.fluid = stack.getFluid();
        this.tag = stack.tag;
        this.hash = Objects.hash(fluid, tag);
    }

    @Override
    @Contract(" -> new")
    public @NotNull FluidStack recombine() {
        return new FluidStack(fluid, 1, tag);
    }

    @Contract("_ -> new")
    public @NotNull FluidStack recombine(int amount) {
        return new FluidStack(fluid, amount, tag);
    }

    @Override
    public boolean test(@Nullable FluidStack stack) {
        return stack != null && stack.getFluid() == fluid && Objects.equals(tag, stack.tag);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FluidTestObject that = (FluidTestObject) o;
        return Objects.equals(fluid, that.fluid) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
