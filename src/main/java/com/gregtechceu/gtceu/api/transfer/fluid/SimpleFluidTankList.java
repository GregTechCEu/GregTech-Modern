package com.gregtechceu.gtceu.api.transfer.fluid;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.NotNull;

public record SimpleFluidTankList(IFluidHandler... handlers) implements IFluidHandler {

    @Override
    public int getTanks() {
        return handlers.length;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int i) {
        return i >= handlers.length ? FluidStack.EMPTY : handlers[i].getFluidInTank(0);
    }

    @Override
    public int getTankCapacity(int i) {
        return i >= handlers.length ? 0 : handlers[i].getTankCapacity(0);
    }

    @Override
    public boolean isFluidValid(int i, @NotNull FluidStack fluidStack) {
        return i < handlers.length && handlers[i].isFluidValid(0, fluidStack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        var copied = resource.copy();
        for (IFluidHandler handler : handlers) {
            var candidate = copied.copy();
            copied.shrink(handler.fill(candidate, action));
            if (copied.isEmpty()) break;
        }
        return resource.getAmount() - copied.getAmount();
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        var copied = resource.copy();
        for (IFluidHandler handler : handlers) {
            var candidate = copied.copy();
            copied.shrink(handler.drain(candidate, action).getAmount());
            if (copied.isEmpty()) break;
        }
        copied.setAmount(resource.getAmount() - copied.getAmount());
        return copied;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain == 0) return FluidStack.EMPTY;
        FluidStack totalDrained = null;
        for (IFluidHandler handler : handlers) {
            if (totalDrained == null || totalDrained.isEmpty()) {
                totalDrained = handler.drain(maxDrain, action);
                if (totalDrained.isEmpty()) totalDrained = null;
                else maxDrain -= totalDrained.getAmount();
            } else {
                FluidStack copy = totalDrained.copy();
                copy.setAmount(maxDrain);
                FluidStack drain = handler.drain(copy, action);
                totalDrained.grow(drain.getAmount());
                maxDrain -= drain.getAmount();
            }
            if (maxDrain <= 0) break;
        }
        return totalDrained == null ? FluidStack.EMPTY : totalDrained;
    }
}
