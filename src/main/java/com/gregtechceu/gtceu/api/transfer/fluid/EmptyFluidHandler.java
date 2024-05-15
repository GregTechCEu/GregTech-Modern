package com.gregtechceu.gtceu.api.transfer.fluid;

import com.lowdragmc.lowdraglib.side.fluid.IFluidHandlerModifiable;

import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

public class EmptyFluidHandler implements IFluidHandlerModifiable {

    public static final EmptyFluidHandler INSTANCE = new EmptyFluidHandler();

    private EmptyFluidHandler() {
        /**/
    }

    @Override
    public void setFluidInTank(int i, FluidStack fluidStack) {}

    @Override
    public int getTanks() {
        return 0;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }
}
