package com.gregtechceu.gtceu.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class MapFluidIngredient extends AbstractMapIngredient {

    public final Fluid fluid;
    public final CompoundTag tag;

    public MapFluidIngredient(FluidStack fluidStack) {
        this.fluid = fluidStack.getFluid();
        this.tag = fluidStack.getTag();
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull FluidIngredient r) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();
        for (FluidStack s : r.getStacks()) {
            list.add(new MapFluidIngredient(s));
        }
        return list;
    }

    @Override
    protected int hash() {
        // the Fluid registered to the fluidName on game load might not be the same Fluid after loading the world, but
        // will still have the same fluidName.
        int hash = 31 + BuiltInRegistries.FLUID.getKey(fluid).hashCode();
        if (tag != null) {
            return 31 * hash + tag.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            MapFluidIngredient other = (MapFluidIngredient) o;
            // the Fluid registered to the fluidName on game load might not be the same Fluid after loading the world,
            // but will still have the same fluidName.
            if (this.fluid.isSame(other.fluid)) {
                return Objects.equals(tag, other.tag);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "MapFluidIngredient{" +
                "{fluid=" + BuiltInRegistries.FLUID.getKey(fluid) + "} {tag=" + tag + "}";
    }
}
