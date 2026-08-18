package com.gregtechceu.gtceu.api.misc.forge;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.NotNull;

public class FluidTankHandler implements IFluidHandler {

    public static IFluidHandler getTankFluidHandler(IFluidTank tank) {
        if (tank instanceof IFluidHandler fluidHandler) {
            return fluidHandler;
        }
        return new FluidTankHandler(tank);
    }

    private final IFluidTank fluidTank;

    public FluidTankHandler(IFluidTank tank) {
        this.fluidTank = tank;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return this.fluidTank.fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        FluidStack currentFluid = this.fluidTank.getFluid();
        if (currentFluid.isEmpty() || !currentFluid.isFluidEqual(resource)) {
            return FluidStack.EMPTY;
        }
        return this.fluidTank.drain(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return this.fluidTank.drain(maxDrain, action);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return this.fluidTank.getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.fluidTank.getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return this.fluidTank.isFluidValid(stack);
    }
}
