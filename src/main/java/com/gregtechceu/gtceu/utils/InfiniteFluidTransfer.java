package com.gregtechceu.gtceu.utils;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class InfiniteFluidTransfer implements IFluidTransfer {

    @Getter
    private final int tanks;

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return FluidStack.empty();
    }

    @Override
    public void setFluidInTank(int tank, @NotNull FluidStack fluidStack) {}

    @Override
    public long getTankCapacity(int tank) {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return true;
    }

    @Override
    public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        return resource.getAmount();
    }

    @Override
    public boolean supportsFill(int tank) {
        return true;
    }

    @NotNull
    @Override
    public FluidStack drain(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        return resource.copy();
    }

    @Override
    public boolean supportsDrain(int tank) {
        return true;
    }

    @NotNull
    @Override
    public Object createSnapshot() {
        return new Object();
    }

    @Override
    public void restoreFromSnapshot(Object snapshot) {}
}
