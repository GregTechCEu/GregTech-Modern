package com.gregtechceu.gtceu.api.transfer.fluid;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class ModifiableFluidHandlerWrapper implements IFluidHandlerModifiable {

    private IFluidHandler handler;

    @Override
    public void setFluidInTank(int tank, FluidStack fluidStack) {
        var fluid = handler.getFluidInTank(tank);
        var canDrain = handler.drain(fluid, FluidAction.SIMULATE);
        if (!canDrain.isEmpty()) {
            drain(canDrain, FluidAction.EXECUTE);
            fill(fluidStack, FluidAction.EXECUTE);
        }
    }

    @Override
    public int getTanks() {
        return handler.getTanks();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return handler.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return handler.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return handler.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return handler.fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        return handler.drain(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return handler.drain(maxDrain, action);
    }
}
