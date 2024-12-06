package com.gregtechceu.gtceu.api.misc.virtualregistry.entries;

import com.gregtechceu.gtceu.api.misc.virtualregistry.EntryTypes;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEntry;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

public class VirtualTank extends VirtualEntry implements IFluidTank, IFluidHandler {
    public static final int DEFAULT_CAPACITY = 64000; // 64B
    protected static final String CAPACITY_KEY = "capacity";
    protected static final String FLUID_KEY = "fluid";
    @NotNull
    private final FluidTank fluidTank;
    private int capacity;
    @Setter
    private Runnable onChange;

    public VirtualTank(int capacity) {
        this.capacity = capacity;
        fluidTank = new FluidTank(this.capacity) {
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                VirtualTank.this.onContentsChanged();
            }
        };
    }

    public VirtualTank() {
        this(DEFAULT_CAPACITY);
    }

    @Override
    public EntryTypes<VirtualTank> getType() {
        return EntryTypes.ENDER_FLUID;
    }

    @NotNull
    @Override
    public FluidStack getFluid() {
        return this.fluidTank.getFluid();
    }

    public void setFluid(FluidStack fluid) {
        this.fluidTank.setFluid(fluid);
    }

    @Override
    public int getFluidAmount() {
        return this.fluidTank.getFluidAmount();
    }

    @Override
    public int getCapacity() {
        return this.capacity;
    }

    @Override
    public boolean isFluidValid(FluidStack fluidStack) {
        return this.fluidTank.isFluidValid(fluidStack);
    }

    @Override
    public int getTanks() {
        return this.fluidTank.getTanks();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int i) {
        return this.fluidTank.getFluidInTank(i);
    }

    @Override
    public int getTankCapacity(int i) {
        if (i != 0) return 0;
        return this.capacity;
    }

    @Override
    public boolean isFluidValid(int i, @NotNull FluidStack fluidStack) {
        return this.fluidTank.isFluidValid(i, fluidStack);
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
        return this.fluidTank.isEmpty();
    }

    @Override
    public int fill(FluidStack fluidStack, FluidAction fluidAction) {
        return this.fluidTank.fill(fluidStack, fluidAction);
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack fluidStack, FluidAction doDrain) {
        return this.fluidTank.drain(fluidStack, doDrain);
    }

    @NotNull
    @Override
    public FluidStack drain(int amount, FluidAction fluidAction) {
        return this.fluidTank.drain(amount, fluidAction);
    }

    protected void onContentsChanged() {
        if (onChange != null) onChange.run();
    }
}
