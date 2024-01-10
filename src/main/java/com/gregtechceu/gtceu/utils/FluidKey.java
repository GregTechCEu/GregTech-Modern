package com.gregtechceu.gtceu.utils;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

import java.util.Objects;

public class FluidKey {

    public final Fluid fluid;
    // Don't make this final, so we can clear the NBT if we remove the only key, resulting in an NBT of {}. Thanks Forge
    public CompoundTag tag;
    private final long amount;

    public FluidKey(FluidStack fluidStack) {
        this.fluid = fluidStack.getFluid();
        this.tag = fluidStack.getTag();
        this.amount = fluidStack.getAmount();
    }

    public FluidKey copy() {
        return new FluidKey(FluidStack.create(getFluid(), this.amount, tag));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FluidKey fluidKey)) return false;
        if (!Objects.equals(fluid, fluidKey.fluid))
            return false;
        if (tag == null && fluidKey.tag != null) return false;
        else return tag == null || tag.equals(fluidKey.tag);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += Objects.hash(fluid);
        if (tag != null && !tag.isEmpty()) {
            hash += tag.hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        return "FluidKey{" +
                "fluid=" + fluid +
                ", tag=" + tag +
                '}';
    }

    public Fluid getFluid() {
        return fluid;
    }
}
