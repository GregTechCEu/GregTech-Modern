package com.gregtechceu.gtceu.utils;

import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Objects;

public class FluidKey {

    @Getter
    public final Holder<Fluid> fluid;
    // Don't make this final, so we can clear the NBT if we remove the only key, resulting in an NBT of {}. Thanks Forge
    public DataComponentPatch component;
    private final int amount;

    public FluidKey(FluidStack fluidStack) {
        this.fluid = fluidStack.getFluidHolder();
        this.component = fluidStack.getComponentsPatch();
        this.amount = fluidStack.getAmount();
    }

    public FluidKey copy() {
        return new FluidKey(new FluidStack(getFluid(), this.amount, component));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FluidKey fluidKey)) return false;
        if (!Objects.equals(fluid, fluidKey.fluid))
            return false;
        if (component == null && fluidKey.component != null) return false;
        else return component == null || component.equals(fluidKey.component);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += Objects.hash(fluid);
        if (component != null && !component.isEmpty()) {
            hash += component.hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        return "FluidKey{" +
                "fluid=" + fluid +
                ", tag=" + component +
                '}';
    }

}
