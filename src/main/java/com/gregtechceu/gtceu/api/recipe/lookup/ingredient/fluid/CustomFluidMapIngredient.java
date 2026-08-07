package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomFluidMapIngredient extends AbstractMapIngredient {

    protected FluidStack stack;
    protected FluidIngredient ingredient = null;

    public CustomFluidMapIngredient(FluidStack stack) {
        this.stack = stack;
    }

    public CustomFluidMapIngredient(FluidStack stack, FluidIngredient ingredient) {
        this.stack = stack;
        this.ingredient = ingredient;
    }

    public static List<AbstractMapIngredient> from(FluidIngredient ingredient) {
        List<AbstractMapIngredient> ingredients = new ArrayList<>();
        FluidStack[] stacks = ingredient.getStacks();
        for (FluidStack stack : stacks) {
            ingredients.add(new CustomFluidMapIngredient(stack, ingredient));
        }
        return ingredients;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(FluidStack stack) {
        return Collections.singletonList(new CustomFluidMapIngredient(stack));
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            CustomFluidMapIngredient other = (CustomFluidMapIngredient) o;
            if (!FluidStack.isSameFluid(this.stack, other.stack)) {
                return false;
            }
            if (this.ingredient != null) {
                if (other.ingredient != null) {
                    return ingredient.equals(other.ingredient);
                } else {
                    return this.ingredient.test(other.stack);
                }
            } else if (other.ingredient != null) {
                return other.ingredient.test(this.stack);
            }
        }
        return false;
    }

    @Override
    protected int hash() {
        return FluidStack.hashFluidAndComponents(stack);
    }

    @Override
    public String toString() {
        return "CustomMapIngredient{" +
                "item=" + stack +
                "ingredient=" + ingredient +
                "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }
}
