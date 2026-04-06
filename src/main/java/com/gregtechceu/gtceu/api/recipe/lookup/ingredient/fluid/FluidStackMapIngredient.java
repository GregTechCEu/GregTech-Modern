package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid;

import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.minecraft.core.Holder;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SingleFluidIngredient;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class FluidStackMapIngredient extends AbstractMapIngredient {

    protected FluidStack stack;

    public FluidStackMapIngredient(Holder<Fluid> fluid) {
        this(new FluidStack(fluid, 1));
    }

    public FluidStackMapIngredient(FluidStack stack) {
        this.stack = stack;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull SingleFluidIngredient ingredient) {
        return Collections.singletonList(new FluidStackMapIngredient(ingredient.fluid()));
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull FluidStack stack) {
        return Collections.singletonList(new FluidStackMapIngredient(stack));
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull IntProviderFluidIngredient ingredient) {
        return Collections.singletonList(new FluidStackMapIngredient(ingredient.getMaxSizeStack()));
    }

    @Override
    protected int hash() {
        return FluidStack.hashFluidAndComponents(stack);
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            FluidStackMapIngredient other = (FluidStackMapIngredient) o;
            return FluidStack.isSameFluid(this.stack, other.stack);
        }
        return false;
    }

    @Override
    public String toString() {
        return "FluidStackMapIngredient{fluid=" + stack + "}";
    }
}
