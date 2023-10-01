package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidStorage;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class OverlayingFluidStorage implements IFluidStorage {
    private final IFluidTransfer transfer;
    private final int tank;

    @Override
    public void onContentsChanged() {
        this.transfer.onContentsChanged();
    }

    @NotNull
    @Override
    public FluidStack getFluid() {
        return transfer.getFluidInTank(tank);
    }

    @Override
    public void setFluid(FluidStack fluid) {
        transfer.setFluidInTank(tank, fluid);
    }

    @Override
    public long getCapacity() {
        return transfer.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(FluidStack stack) {
        return transfer.isFluidValid(tank, stack);
    }

    @Override
    public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        if (transfer instanceof NotifiableFluidTank notifiable) {
            return notifiable.storages[this.tank].fill(resource, simulate, notifyChanges);
        }
        return transfer.fill(this.tank, resource, simulate, notifyChanges);
    }

    @Override
    public boolean supportsFill(int tank) {
        return transfer.supportsFill(this.tank);
    }

    @NotNull
    @Override
    public FluidStack drain(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        if (transfer instanceof NotifiableFluidTank notifiable) {
            return notifiable.storages[this.tank].drain(resource, simulate, notifyChanges);
        }
        return transfer.drain(this.tank, resource, simulate, notifyChanges);
    }

    @Override
    public boolean supportsDrain(int tank) {
        return transfer.supportsDrain(this.tank);
    }

    @NotNull
    @Override
    public Object createSnapshot() {
        return transfer.createSnapshot();
    }

    @Override
    public void restoreFromSnapshot(Object snapshot) {
        transfer.restoreFromSnapshot(snapshot);
    }
}
