package com.gregtechceu.gtceu.utils.virtualregistry.entries;

import com.gregtechceu.gtceu.utils.virtualregistry.EntryTypes;
import com.gregtechceu.gtceu.utils.virtualregistry.VirtualEntry;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class VirtualTank extends VirtualEntry implements IFluidTank, IFluidHandler {
    protected  static final String CAPACITY_KEY = "capacity";
    protected  static final String FLUID_KEY = "fluid";
    private static final int DEFAULT_CAPACITY = 64000;

    @Setter
    FluidStack fluidStack = null;
    private int capacity;

    public VirtualTank(int capacity) {
        this.capacity = capacity;
    }

    public VirtualTank() {
        this(DEFAULT_CAPACITY);
    }

    @Override
    public EntryTypes<VirtualTank> getType() {
        return EntryTypes.ENDER_FLUID;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof  VirtualTank other)) return false;
        if(this.fluidStack == null && other.fluidStack == null)
            return super.equals(obj);
        if(this.fluidStack == null || other.fluidStack == null)
            return false;
        if(this.fluidStack.isFluidStackIdentical(other.fluidStack))
            return super.equals(obj);
        return false;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = super.serializeNBT();
        tag.putInt(CAPACITY_KEY, this.capacity);

        if(this.fluidStack != null)
            tag.put(FLUID_KEY, this.fluidStack.writeToNBT(new CompoundTag()));

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        this.capacity = tag.getInt(CAPACITY_KEY);

        if(tag.contains(FLUID_KEY))
            setFluidStack(FluidStack.loadFluidStackFromNBT(tag.getCompound(FLUID_KEY)));
    }

    @Override
    @NotNull
    public FluidStack getFluid() {
        return this.fluidStack;
    }

    @Override
    public int getFluidAmount() {
        return this.fluidStack.getAmount();
    }

    @Override
    public int getCapacity() {
        return this.capacity;
    }

    @Override
    public boolean isFluidValid(net.minecraftforge.fluids.FluidStack fluidStack) {
        return true;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    @NotNull
    public FluidStack getFluidInTank(int i) {
        return this.fluidStack;
    }

    @Override
    public int getTankCapacity(int i) {
        return this.capacity;
    }

    @Override
    @NotNull
    public boolean isFluidValid(int i, FluidStack fluidStack) {
        return false;
    }

    @Override
    public int fill(FluidStack fluidStack, FluidAction fluidAction) {
        if(fluidStack == null || fluidStack.isEmpty() || (this.fluidStack != null && !fluidStack.isFluidEqual(this.fluidStack))) {
            return 0;
        }

        int fillAmt = Math.min(fluidStack.getAmount(), getCapacity() - this.getFluidAmount());

        if(fluidAction.execute()) {
            if(this.fluidStack == null) {
                this.fluidStack = new FluidStack(fluidStack, fillAmt);
            } else {
                this.fluidStack.setAmount(this.fluidStack.getAmount() + fillAmt);
            }
        }
        return fillAmt;
    }

    @Override
    @NotNull
    public FluidStack drain(int i, FluidAction fluidAction) {
        if(this.fluidStack == null || i <= 0) {
            return null;
        }

        int drainAmt = Math.min(this.getFluidAmount(), i);
        FluidStack drainedFluid = new FluidStack(this.fluidStack, drainAmt);
        if(fluidAction.execute()) {
            this.fluidStack.setAmount(this.fluidStack.getAmount() - drainAmt);
            if(this.fluidStack.getAmount() <= 0) {
                this.fluidStack = null;
            }
        }
        return drainedFluid;
    }

    @Override
    @NotNull
    public FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
        return fluidStack == null || !fluidStack.isFluidEqual(this.fluidStack) ? null : drain(fluidStack.getAmount(), fluidAction);
    }
}
