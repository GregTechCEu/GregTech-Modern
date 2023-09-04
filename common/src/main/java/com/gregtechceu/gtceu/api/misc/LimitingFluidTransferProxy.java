package com.gregtechceu.gtceu.api.misc;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class LimitingFluidTransferProxy implements IFluidTransfer {
    private final IFluidTransfer delegate;

    @Getter @Setter
    private long remainingTransfer;

    public LimitingFluidTransferProxy(IFluidTransfer delegate, long transferLimit) {
        this.delegate = delegate;
        this.remainingTransfer = transferLimit;
    }

    @NotNull
    private FluidStack copyLimited(FluidStack resource) {
        var limitedResource = resource.copy();
        limitedResource.setAmount(Math.min(limitedResource.getAmount(), remainingTransfer));

        return limitedResource;
    }

    /////////////////////////////////////
    //********    OVERRIDES    ********//
    /////////////////////////////////////

    @Override
    @ApiStatus.Internal
    public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        var filled = delegate.fill(tank, copyLimited(resource), simulate, notifyChanges);

        if (!simulate) {
            remainingTransfer -= filled;
        }

        return filled;
    }

    @Override
    public long fill(FluidStack resource, boolean simulate, boolean notifyChanges) {
        var filled = delegate.fill(copyLimited(resource), simulate, notifyChanges);

        if (!simulate) {
            remainingTransfer -= filled;
        }

        return filled;
    }

    @Override
    public long fill(FluidStack resource, boolean simulate) {
        var filled = delegate.fill(copyLimited(resource), simulate);

        if (!simulate) {
            remainingTransfer -= filled;
        }

        return filled;
    }


    @Override
    @ApiStatus.Internal
    @Nonnull
    public FluidStack drain(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        var drained = delegate.drain(tank, copyLimited(resource), simulate, notifyChanges);

        if (!simulate && !drained.isEmpty()) {
            remainingTransfer -= drained.getAmount();
        }

        return drained;
    }

    @Override
    @Nonnull
    public FluidStack drain(FluidStack resource, boolean simulate, boolean notifyChanges) {
        var drained = delegate.drain(copyLimited(resource), simulate, notifyChanges);

        if (!simulate && !drained.isEmpty()) {
            remainingTransfer -= drained.getAmount();
        }

        return drained;
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean simulate) {
        var drained = delegate.drain(copyLimited(resource), simulate);

        if (!simulate && !drained.isEmpty()) {
            remainingTransfer -= drained.getAmount();
        }

        return drained;
    }

    @Override
    @Nonnull
    public FluidStack drain(long maxDrain, boolean simulate, boolean notifyChanges) {
        var drained = delegate.drain(Math.min(maxDrain, remainingTransfer), simulate, notifyChanges);

        if (!simulate && !drained.isEmpty()) {
            remainingTransfer -= drained.getAmount();
        }

        return drained;
    }

    @Override
    public FluidStack drain(long maxDrain, boolean simulate) {
        var drained = delegate.drain(Math.min(maxDrain, remainingTransfer), simulate);

        if (!simulate && !drained.isEmpty()) {
            remainingTransfer -= drained.getAmount();
        }

        return drained;
    }


    ////////////////////////////////////////////    Delegated as is:    ////////////////////////////////////////////

    @Override
    public int getTanks() {
        return delegate.getTanks();
    }

    @Override
    @Nonnull
    public FluidStack getFluidInTank(int i) {
        return delegate.getFluidInTank(i);
    }

    @Override
    @ApiStatus.Internal
    public void setFluidInTank(int i, @NotNull FluidStack fluidStack) {
        delegate.setFluidInTank(i, fluidStack);
    }

    @Override
    public long getTankCapacity(int i) {
        return delegate.getTankCapacity(i);
    }

    @Override
    public boolean isFluidValid(int i, @NotNull FluidStack fluidStack) {
        return delegate.isFluidValid(i, fluidStack);
    }

    @Override
    public boolean supportsFill(int i) {
        return delegate.supportsFill(i);
    }

    @Override
    public boolean supportsDrain(int i) {
        return delegate.supportsDrain(i);
    }

    @Override
    public void onContentsChanged() {
        delegate.onContentsChanged();
    }

    @Override
    @ApiStatus.Internal
    @Nonnull
    public Object createSnapshot() {
        return delegate.createSnapshot();
    }

    @Override
    @ApiStatus.Internal
    public void restoreFromSnapshot(Object o) {
        delegate.restoreFromSnapshot(o);
    }
}
