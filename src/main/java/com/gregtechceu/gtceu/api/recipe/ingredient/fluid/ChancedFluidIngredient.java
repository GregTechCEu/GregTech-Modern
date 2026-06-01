package com.gregtechceu.gtceu.api.recipe.ingredient.fluid;

import com.gregtechceu.gtceu.api.recipe.ingredient.IChancedIngredient;

import net.minecraftforge.fluids.FluidStack;

import lombok.Getter;
import org.jetbrains.annotations.Range;

public final class ChancedFluidIngredient extends FluidIngredient implements IChancedIngredient {

    @Getter
    private final FluidIngredient inner;
    private final int chance;
    @Getter
    private final int multiplier;

    private ChancedFluidIngredient(FluidIngredient ingredient, @Range(from = 0, to = 10000) int chance,
                           int multiplier) {
        super(ingredient.amount * multiplier);
        this.inner = ingredient;
        this.chance = chance;
        this.multiplier = multiplier;
    }

    public ChancedFluidIngredient(FluidIngredient ingredient, int chance) {
        this(ingredient, chance, 1);
    }

    @Override
    public int hash() {
        return inner.hash();
    }

    @Override
    public FluidStack[] getStacks() {
        return inner.getStacks();
    }

    @Override
    public FluidStack toStack() {
        FluidStack[] stacks = getStacks();
        if (stacks.length == 0) return FluidStack.EMPTY;
        FluidStack stack = stacks[0].copy();
        stack.setAmount(inner.getAmount() * IChancedIngredient.rollSuccesses(multiplier, chance));
        return stack;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return inner.test(fluidStack);
    }

    @Override
    public boolean isChanced() {
        return true;
    }

    @Override
    public boolean isRanged() {
        return inner.isRanged();
    }

    @Override
    public int getChance() {
        return chance;
    }

    @Override
    public ChancedFluidIngredient copy() {
        return new ChancedFluidIngredient(inner.copy(), chance, multiplier);
    }

    @Override
    public ChancedFluidIngredient copyWithMultiplier(int multiplier) {
        return new ChancedFluidIngredient(inner.copy(), chance, this.multiplier * multiplier);
    }

    @Override
    public ChancedFluidIngredient copyWithChance(int chance) {
        return new ChancedFluidIngredient(inner.copy(), chance, multiplier);
    }
}
