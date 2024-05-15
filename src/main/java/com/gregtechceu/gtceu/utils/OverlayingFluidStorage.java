package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;

import com.lowdragmc.lowdraglib.side.fluid.IFluidHandlerModifiable;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class OverlayingFluidStorage implements IFluidHandlerModifiable, IFluidTank {

    private final IFluidHandlerModifiable transfer;
    private final int tank;

    @NotNull
    @Override
    public FluidStack getFluid() {
        return transfer.getFluidInTank(tank);
    }

    @Override
    public int getFluidAmount() {
        return getFluid().getAmount();
    }

    @Override
    public int getCapacity() {
        return transfer.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(FluidStack stack) {
        return transfer.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (transfer instanceof NotifiableFluidTank notifiable) {
            return notifiable.getStorages()[this.tank].fill(resource, action);
        }
        return transfer.fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (transfer instanceof NotifiableFluidTank notifiable) {
            return notifiable.getStorages()[this.tank].drain(resource, action);
        }
        return transfer.drain(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (transfer instanceof NotifiableFluidTank notifiable) {
            return notifiable.getStorages()[this.tank].drain(maxDrain, action);
        }
        return transfer.drain(maxDrain, action);
    }

    @Override
    public void setFluidInTank(int tank, FluidStack stack) {
        transfer.setFluidInTank(tank, stack);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return transfer.isFluidValid(tank, stack);
    }
}
