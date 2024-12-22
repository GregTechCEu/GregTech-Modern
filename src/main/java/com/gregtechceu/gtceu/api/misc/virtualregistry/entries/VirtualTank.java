package com.gregtechceu.gtceu.api.misc.virtualregistry.entries;

import com.gregtechceu.gtceu.api.misc.virtualregistry.EntryTypes;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEntry;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class VirtualTank extends VirtualEntry {

    public static final int DEFAULT_CAPACITY = 160_000; // 160B for per second transfer
    protected static final String CAPACITY_KEY = "capacity";
    protected static final String FLUID_KEY = "fluid";
    @NotNull
    @Getter
    private final FluidTank fluidTank;
    private int capacity;

    public VirtualTank(int capacity) {
        this.capacity = capacity;
        fluidTank = new FluidTank(this.capacity);
    }

    public VirtualTank() {
        this(DEFAULT_CAPACITY);
    }

    @Override
    public EntryTypes<VirtualTank> getType() {
        return EntryTypes.ENDER_FLUID;
    }

    public void setFluid(FluidStack fluid) {
        this.fluidTank.setFluid(fluid);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VirtualTank other)) return false;
        return this.fluidTank == other.fluidTank;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = super.serializeNBT();
        tag.putInt(CAPACITY_KEY, this.capacity);

        if (this.fluidTank.getFluid() != FluidStack.EMPTY)
            tag.put(FLUID_KEY, this.fluidTank.getFluid().writeToNBT(new CompoundTag()));

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        this.capacity = nbt.getInt(CAPACITY_KEY);

        if (nbt.contains(FLUID_KEY))
            setFluid(FluidStack.loadFluidStackFromNBT(nbt.getCompound(FLUID_KEY)));
    }

    @Override
    public boolean canRemove() {
        return super.canRemove() && this.fluidTank.isEmpty();
    }
}
