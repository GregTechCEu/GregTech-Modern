package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid;

import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MapFluidStackDataComponentIngredient extends MapFluidStackIngredient {

    protected FluidIngredient nbtIngredient;

    public MapFluidStackDataComponentIngredient(FluidStack s, FluidIngredient nbtIngredient) {
        super(s);
        this.nbtIngredient = nbtIngredient;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull FluidIngredient r) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();
        for (FluidStack s : r.getStacks()) {
            list.add(new MapFluidStackDataComponentIngredient(s, r));
        }
        return list;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MapFluidStackDataComponentIngredient other) {
            if (this.stack.getFluid() != other.stack.getFluid()) {
                return false;
            }
            if (this.nbtIngredient != null) {
                if (other.nbtIngredient != null) {
                    return FluidStack.isSameFluidSameComponents(nbtIngredient.getStacks()[0],
                            other.nbtIngredient.getStacks()[0]);
                }
            } else if (other.nbtIngredient != null) {
                return other.nbtIngredient.test(this.stack);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "MapFluidStackDataComponentIngredient{" + "fluid=" + BuiltInRegistries.FLUID.getKey(stack.getFluid()) +
                "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }
}
