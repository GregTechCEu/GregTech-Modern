package com.gregtechceu.gtceu.utils;

import com.lowdragmc.lowdraglib.side.fluid.IFluidHandlerModifiable;

import net.neoforged.neoforge.fluids.FluidStack;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class InfiniteFluidTransfer implements IFluidHandlerModifiable {

    @Getter
    private final int tanks;

    @Override
    public void setFluidInTank(int tank, @NotNull FluidStack fluidStack) {}

    @Override
    public FluidStack getFluidInTank(int tank) {
        return null;
    }

    @Override
    public int getTankCapacity(int tank) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return resource;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return true;
    }
}
