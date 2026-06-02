package com.gregtechceu.gtceu.api.recipe.ingredient.fluid;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraftforge.fluids.FluidStack;

import lombok.Getter;

public final class RangedFluidIngredient extends FluidIngredient {

    @Getter
    private final FluidIngredient inner;
    @Getter
    private final int minAmount;
    private final IntProvider amountProvider;

    RangedFluidIngredient(FluidIngredient ingredient, int minAmount, int maxAmount) {
        super(maxAmount);
        this.inner = ingredient;
        this.minAmount = minAmount;
        this.amountProvider = UniformInt.of(minAmount, maxAmount);
    }

    @Override
    public int hash() {
        return inner.hash();
    }

    @Override
    public FluidStack[] getFluids() {
        return inner.getFluids();
    }

    @Override
    public FluidStack toStack() {
        FluidStack[] stacks = getFluids();
        if (stacks.length == 0) return FluidStack.EMPTY;
        FluidStack stack = stacks[0].copy();
        stack.setAmount(amountProvider.sample(GTValues.RNG));
        return stack;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return inner.test(fluidStack);
    }

    @Override
    public boolean isRanged() {
        return true;
    }

    @Override
    public RangedFluidIngredient copy() {
        return new RangedFluidIngredient(inner.copy(), minAmount, amount);
    }

    @Override
    public FluidIngredient copyWithAmount(int amount) {
        return inner.copyWithAmount(amount);
    }

    @Override
    public RangedFluidIngredient copyWithMultiplier(int multiplier) {
        return new RangedFluidIngredient(inner.copy(), minAmount * multiplier, amount * multiplier);
    }

    @Override
    public ChancedFluidIngredient copyWithChance(int chance) {
        return new ChancedFluidIngredient(copy(), chance);
    }
}
