package com.gregtechceu.gtceu.api.transfer.fluid;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;

import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class FluidHandlerDelegate implements IFluidHandlerModifiable {

    @Setter
    public IFluidHandlerModifiable delegate;

    public FluidHandlerDelegate(IFluidHandlerModifiable delegate) {
        this.delegate = delegate;
    }

    //////////////////////////////////////
    // ****** OVERRIDE THESE ******//
    //////////////////////////////////////

    @Override
    public int getTanks() {
        return delegate.getTanks();
    }

    @Override
    @NotNull
    public FluidStack getFluidInTank(int tank) {
        return delegate.getFluidInTank(tank);
    }

    @Override
    @ApiStatus.Internal
    public void setFluidInTank(int tank, @NotNull FluidStack fluidStack) {
        delegate.setFluidInTank(tank, fluidStack);
    }

    @Override
    public int getTankCapacity(int tank) {
        return delegate.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return delegate.isFluidValid(tank, stack);
    }

    @Override
    @ApiStatus.Internal
    public int fill(FluidStack resource, FluidAction action) {
        return delegate.fill(resource, action);
    }

    @Override
    public boolean supportsFill(int tank) {
        return delegate.supportsFill(tank);
    }

    @Override
    @ApiStatus.Internal
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return delegate.drain(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return delegate.drain(maxDrain, action);
    }

    @Override
    public boolean supportsDrain(int tank) {
        return delegate.supportsDrain(tank);
    }
}
