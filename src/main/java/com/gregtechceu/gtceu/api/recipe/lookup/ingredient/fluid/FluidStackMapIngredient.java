package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class FluidStackMapIngredient extends AbstractMapIngredient {

    protected FluidStack stack;
    protected FluidIngredient ingredient = null;

    public FluidStackMapIngredient(FluidStack stack) {
        this.stack = stack;
    }

    public FluidStackMapIngredient(FluidStack stack, FluidIngredient ingredient) {
        this.stack = stack;
        this.ingredient = ingredient;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull FluidIngredient ingredient) {
        List<AbstractMapIngredient> ingredients = new ObjectArrayList<>();
        for (FluidIngredient.Value value : ingredient.values) {
            if (value instanceof FluidIngredient.FluidValue fluidValue) {
                FluidStack stack = new FluidStack(fluidValue.fluid(),
                        // wait. that's illegal.
                        (ingredient instanceof IntProviderFluidIngredient provider ?
                                provider.getCountProvider().getMaxValue() :
                                ingredient.getAmount()),
                        ingredient.getNbt());
                ingredients.add(new FluidStackMapIngredient(stack, ingredient));
            }
        }
        return ingredients;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull FluidStack stack) {
        return Collections.singletonList(new FluidStackMapIngredient(stack));
    }

    @Override
    protected int hash() {
        return stack.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            FluidStackMapIngredient other = (FluidStackMapIngredient) o;
            if (this.stack.getFluid() != other.stack.getFluid()) {
                return false;
            }
            if (this.ingredient != null) {
                if (other.ingredient != null) {
                    return this.ingredient.equals(other.ingredient);
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
    public String toString() {
        return "FluidStackMapIngredient{fluid=" + stack + "}";
    }
}
