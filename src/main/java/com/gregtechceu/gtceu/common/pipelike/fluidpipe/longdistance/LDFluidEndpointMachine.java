package com.gregtechceu.gtceu.common.pipelike.fluidpipe.longdistance;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.common.machine.storage.LongDistanceEndpointMachine;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LDFluidEndpointMachine extends LongDistanceEndpointMachine {

    public LDFluidEndpointMachine(IMachineBlockEntity holder) {
        super(holder, LDFluidPipeType.INSTANCE);
    }

    @Override
    public @Nullable IFluidHandlerModifiable getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        if (isRemote() || getIoType() != IO.IN || side != getFrontFacing()) {
            return null;
        }
        var endpoint = getLink();
        if (endpoint == null) {
            return null;
        }
        return GTTransferUtils.getAdjacentFluidHandler(getLevel(), endpoint.getPos(), endpoint.getOutputFacing())
                .map(LDFluidEndpointMachine.FluidHandlerWrapper::new)
                .orElse(null);
    }

    public static class FluidHandlerWrapper implements IFluidHandlerModifiable {

        private final IFluidHandler delegate;

        public FluidHandlerWrapper(IFluidHandler delegate) {
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
            if (delegate instanceof IFluidHandlerModifiable modifiable) {
                modifiable.setFluidInTank(tank, fluidStack);
            }
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
        public boolean supportsFill(int tank) {
            if (delegate instanceof IFluidHandlerModifiable modifiable) {
                return modifiable.supportsFill(tank);
            }
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return delegate.fill(resource, action);
        }

        @Override
        public boolean supportsDrain(int tank) {
            return false;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}
