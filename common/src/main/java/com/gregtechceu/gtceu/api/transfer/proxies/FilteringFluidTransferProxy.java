package com.gregtechceu.gtceu.api.transfer.proxies;

import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FilteringFluidTransferProxy implements IFluidTransfer {
    private final IFluidTransfer delegate;

    @Getter
    private FluidFilter filter;

    public FilteringFluidTransferProxy(IFluidTransfer delegate, FluidFilter filter) {
        this.delegate = delegate;
        this.filter = filter;
    }

    public void setFilter(@Nullable FluidFilter filter) {
        this.filter = filter != null ? filter : FluidFilter.EMPTY;
    }

    private FluidStack findFirstAllowedFluid(long amount) {
        for (int tank = 0; tank < getTanks(); tank++) {
            FluidStack fluid = getFluidInTank(tank);

            if (!fluid.isEmpty() && this.filter.test(fluid))
                return fluid.copy(amount);
        }

        return FluidStack.empty();
    }

    @Override
    @ApiStatus.Internal
    public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        if (!filter.test(resource))
            return 0L;

        return delegate.fill(tank, resource, simulate, notifyChanges);
    }

    @Override
    public long fill(FluidStack resource, boolean simulate, boolean notifyChanges) {
        if (!filter.test(resource))
            return 0L;

        return delegate.fill(resource, simulate, notifyChanges);
    }

    @Override
    public long fill(FluidStack resource, boolean simulate) {
        if (!filter.test(resource))
            return 0L;

        return delegate.fill(resource, simulate);
    }

    @Override
    @Nonnull
    public FluidStack drain(FluidStack resource, boolean simulate, boolean notifyChanges) {
        var drained = delegate.drain(resource, true, notifyChanges);

        if (!this.filter.test(drained))
            return FluidStack.empty();

        if (!simulate) {
            drained = delegate.drain(resource, false, notifyChanges);
        }

        return drained;
    }

    @Override
    @ApiStatus.Internal
    @Nonnull
    public FluidStack drain(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        var drained = delegate.drain(tank, resource, true, notifyChanges);

        if (!this.filter.test(drained))
            return FluidStack.empty();

        if (!simulate) {
            drained = delegate.drain(tank, resource, false, notifyChanges);
        }

        return drained;
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean simulate) {
        var drained = delegate.drain(resource, true);

        if (!this.filter.test(drained))
            return FluidStack.empty();

        if (!simulate) {
            drained = delegate.drain(resource, false);
        }

        return drained;
    }

    @Override
    @Nonnull
    public FluidStack drain(long maxDrain, boolean simulate, boolean notifyChanges) {
        var allowedFluid = findFirstAllowedFluid(maxDrain);

        if (allowedFluid.isEmpty())
            return FluidStack.empty();

        return delegate.drain(allowedFluid, simulate, notifyChanges);
    }

    @Override
    public FluidStack drain(long maxDrain, boolean simulate) {
        var allowedFluid = findFirstAllowedFluid(maxDrain);

        if (allowedFluid.isEmpty())
            return FluidStack.empty();

        return delegate.drain(allowedFluid, simulate);
    }

    ////////////////////////////////////////////    Delegated as is:    ////////////////////////////////////////////

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
    public boolean supportsFill(int tank) {
        return delegate.supportsFill(tank);
    }

    @Override
    public boolean supportsDrain(int tank) {
        return delegate.supportsDrain(tank);
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
    public void restoreFromSnapshot(Object snapshot) {
        delegate.restoreFromSnapshot(snapshot);
    }
}
