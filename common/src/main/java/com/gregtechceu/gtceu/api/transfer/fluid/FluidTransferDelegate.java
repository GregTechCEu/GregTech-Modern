package com.gregtechceu.gtceu.api.transfer.fluid;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class FluidTransferDelegate implements IFluidTransfer {
    protected IFluidTransfer delegate;

    public FluidTransferDelegate(IFluidTransfer delegate) {
        this.delegate = delegate;
    }

    protected void setDelegate(IFluidTransfer delegate) {
        this.delegate = delegate;
    }


    //////////////////////////////////////
    //******    OVERRIDE THESE    ******//
    //////////////////////////////////////


    @Override
    public int getTanks() {
        return delegate.getTanks();
    }

    @Override
    @Nonnull
    public FluidStack getFluidInTank(int tank) {
        return delegate.getFluidInTank(tank);
    }

    @Override
    @ApiStatus.Internal
    public void setFluidInTank(int tank, @NotNull FluidStack fluidStack) {
        delegate.setFluidInTank(tank, fluidStack);
    }

    @Override
    public long getTankCapacity(int tank) {
        return delegate.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return delegate.isFluidValid(tank, stack);
    }

    @Override
    @ApiStatus.Internal
    public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        return delegate.fill(tank, resource, simulate, notifyChanges);
    }

    @Override
    public boolean supportsFill(int tank) {
        return delegate.supportsFill(tank);
    }

    @Override
    @ApiStatus.Internal
    @Nonnull
    public FluidStack drain(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        return delegate.drain(tank, resource, simulate, notifyChanges);
    }

    @Override
    public boolean supportsDrain(int tank) {
        return delegate.supportsDrain(tank);
    }

    @Override
    @ApiStatus.Internal
    @Nonnull
    public Object createSnapshot() {
        return delegate.createSnapshot();
    }

    @Override
    @ApiStatus.Internal
    public void restoreFromSnapshot(Object snapshot) {
        delegate.restoreFromSnapshot(snapshot);
    }

    @Override
    public void onContentsChanged() {
        delegate.onContentsChanged();
    }
}
