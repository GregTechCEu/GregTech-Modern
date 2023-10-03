package com.gregtechceu.gtceu.common.pipelike.fluidpipe.longdistance;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.storage.LongDistanceEndpointMachine;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import org.jetbrains.annotations.NotNull;

public class LDFluidEndpointMachine extends LongDistanceEndpointMachine {

    public LDFluidEndpointMachine(IMachineBlockEntity holder) {
        super(holder, LDFluidPipeType.INSTANCE);
    }

    public static class FluidHandlerWrapper implements IFluidTransfer {

        private final IFluidTransfer delegate;

        public FluidHandlerWrapper(IFluidTransfer delegate) {
            this.delegate = delegate;
        }

        @Override
        public int getTanks() {
            return delegate.getTanks();
        }

        @NotNull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return delegate.getFluidInTank(tank);
        }

        @Override
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
        public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
            return delegate.fill(resource, simulate, notifyChanges);
        }

        @Override
        public boolean supportsFill(int tank) {
            return delegate.supportsFill(tank);
        }

        @NotNull
        @Override
        public FluidStack drain(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
            return FluidStack.empty();
        }

        @Override
        public boolean supportsDrain(int tank) {
            return false;
        }

        @NotNull
        @Override
        public Object createSnapshot() {
            return delegate.createSnapshot();
        }

        @Override
        public void restoreFromSnapshot(Object snapshot) {
            delegate.restoreFromSnapshot(snapshot);
        }
    }
}