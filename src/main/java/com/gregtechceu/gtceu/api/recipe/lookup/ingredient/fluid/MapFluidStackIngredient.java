package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid;

import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MapFluidStackIngredient extends AbstractMapIngredient {

    protected FluidStack stack;
    protected FluidIngredient ingredient = null;

    public MapFluidStackIngredient(FluidStack fluidStack) {
        this.stack = fluidStack;
    }

    public MapFluidStackIngredient(FluidStack stack, FluidIngredient ingredient) {
        this.stack = stack;
        this.ingredient = ingredient;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull SizedFluidIngredient r) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();
        for (FluidStack s : r.getFluids()) {
            list.add(new MapFluidStackIngredient(s));
        }
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            MapFluidStackIngredient other = (MapFluidStackIngredient) o;
            if (this.stack.getFluid() != other.stack.getFluid()) {
                return false;
            }
            if (this.ingredient != null) {
                if (other.ingredient != null) {
                    return this.ingredient.equals(other.ingredient);
                }
            } else if (other.ingredient != null) {
                return other.ingredient.test(this.stack);
            }
        }
        return false;
    }

    @Override
    protected int hash() {
        return stack.getFluid().hashCode() * 31;
    }

    @Override
    public String toString() {
        return "MapFluidStackIngredient{" + "fluid=" + BuiltInRegistries.FLUID.getKey(stack.getFluid()) + "}";
    }
}
