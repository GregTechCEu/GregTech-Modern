package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FluidDataComponentMapIngredient extends FluidStackMapIngredient {

    protected DataComponentFluidIngredient componentIngredient;

    public FluidDataComponentMapIngredient(FluidStack s, DataComponentFluidIngredient componentIngredient) {
        super(s.getFluidHolder());
        this.componentIngredient = componentIngredient;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull DataComponentFluidIngredient r) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();
        for (FluidStack s : r.getStacks()) {
            list.add(new FluidDataComponentMapIngredient(s, r));
        }
        return list;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull FluidStack stack) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();

        FluidIngredient strict = DataComponentFluidIngredient.of(true, stack);
        list.add(new FluidDataComponentMapIngredient(stack, (DataComponentFluidIngredient) strict));

        FluidIngredient partial = DataComponentFluidIngredient.of(false, stack);
        list.add(new FluidDataComponentMapIngredient(stack, (DataComponentFluidIngredient) partial));

        return list;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof FluidDataComponentMapIngredient other) {
            if (!FluidStack.isSameFluid(this.stack, other.stack)) {
                return false;
            }
            if (this.componentIngredient == other.componentIngredient) {
                return true;
            }

            if (this.componentIngredient != null) {
                if (other.componentIngredient != null) {
                    if (this.componentIngredient.isStrict() != other.componentIngredient.isStrict()) {
                        return false;
                    }
                    if (!this.componentIngredient.components().equals(other.componentIngredient.components())) {
                        return false;
                    }

                    if (this.componentIngredient.isStrict()) {
                        for (FluidStack tStack : this.componentIngredient.getStacks()) {
                            for (FluidStack oStack : other.componentIngredient.getStacks()) {
                                if (FluidStack.isSameFluidSameComponents(tStack, oStack)) return true;
                            }
                        }
                    } else {
                        boolean thisContains = this.componentIngredient.fluids()
                                .stream().allMatch(holder -> other.componentIngredient.fluids().contains(holder));
                        boolean otherContains = other.componentIngredient.fluids()
                                .stream().allMatch(holder -> this.componentIngredient.fluids().contains(holder));
                        return thisContains && otherContains;
                    }
                } else {
                    return this.componentIngredient.test(other.stack);
                }
            } else {
                return other.componentIngredient.test(this.stack);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "MapFluidStackDataComponentIngredient{fluid=" + BuiltInRegistries.FLUID.getKey(stack.getFluid()) + "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }

    protected int hash() {
        return componentIngredient.hashCode();
    }
}
