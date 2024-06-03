package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid;

import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MapFluidStackWeakDataComponentIngredient extends MapFluidStackIngredient {

    FluidIngredient nbtIngredient;

    public MapFluidStackWeakDataComponentIngredient(FluidStack stack, FluidIngredient nbtIngredient) {
        super(stack, nbtIngredient);
        this.nbtIngredient = nbtIngredient;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull FluidIngredient r) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();
        for (FluidStack s : r.getStacks()) {
            list.add(new MapFluidStackWeakDataComponentIngredient(s, r));
        }
        return list;
    }

    @Override
    protected int hash() {
        return stack.getFluid().hashCode() * 31;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MapFluidStackWeakDataComponentIngredient other) {
            if (this.stack.getFluid() != other.stack.getFluid()) {
                return false;
            }
            if (this.nbtIngredient != null) {
                if (other.nbtIngredient != null) {
                    if (this.nbtIngredient.getStacks().length != other.nbtIngredient.getStacks().length)
                        return false;
                    for (FluidStack stack : this.nbtIngredient.getStacks()) {
                        if (!other.nbtIngredient.test(stack)) {
                            return false;
                        }
                    }
                    return true;
                }
            } else if (other.nbtIngredient != null) {
                return other.nbtIngredient.test(this.stack);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "MapFluidStackWeakDataComponentIngredient{" + "item=" +
                BuiltInRegistries.FLUID.getKey(stack.getFluid()) +
                "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }
}
