package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class OverlayingFluidStorage implements IFluidHandlerModifiable, IFluidTank {

    private final IFluidHandlerModifiable handler;
    private final int tank;

    @Override
    public @NotNull FluidStack getFluid() {
        return handler.getFluidInTank(tank);
    }

    @Override
    public int getFluidAmount() {
        return getFluid().getAmount();
    }

    @Override
    public int getCapacity() {
        return handler.getTankCapacity(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return getCapacity();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return getFluid();
    }

    @Override
    public void setFluidInTank(int tank, FluidStack stack) {
        handler.setFluidInTank(tank, stack);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public boolean isFluidValid(FluidStack stack) {
        return isFluidValid(tank, stack);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return handler.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (handler instanceof NotifiableFluidTank notifiable) {
            return notifiable.getStorages()[this.tank].fill(resource, action);
        }
        return handler.fill(resource, action);
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (handler instanceof NotifiableFluidTank notifiable) {
            return notifiable.getStorages()[this.tank].drain(resource, action);
        }
        return handler.drain(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (handler instanceof NotifiableFluidTank notifiable) {
            return notifiable.getStorages()[this.tank].drain(maxDrain, action);
        }
        return handler.drain(maxDrain, action);
    }
}
