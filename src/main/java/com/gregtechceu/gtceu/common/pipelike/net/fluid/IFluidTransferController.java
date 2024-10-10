package com.gregtechceu.gtceu.common.pipelike.net.fluid;

import com.gregtechceu.gtceu.api.graphnet.pipenet.transfer.TransferControl;
import com.gregtechceu.gtceu.api.graphnet.pipenet.transfer.TransferControlProvider;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.FluidTestObject;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IFluidTransferController {

    TransferControl<IFluidTransferController> CONTROL = new TransferControl<>("Fluid") {

        @Override
        public @NotNull IFluidTransferController get(@Nullable Object potentialHolder) {
            if (!(potentialHolder instanceof TransferControlProvider holder)) return DEFAULT;
            IFluidTransferController found = holder.getControllerForControl(CONTROL);
            return found == null ? DEFAULT : found;
        }

        @Override
        public @NotNull IFluidTransferController getNoPassage() {
            return NO_PASSAGE;
        }
    };

    IFluidTransferController DEFAULT = new IFluidTransferController() {};

    IFluidTransferController NO_PASSAGE = new IFluidTransferController() {

        @Override
        public long insertToHandler(@NotNull FluidTestObject testObject, long amount,
                                    @NotNull IFluidTransfer destHandler, boolean doFill) {
            return 0;
        }

        @Override
        public @Nullable FluidStack extractFromHandler(@Nullable FluidTestObject testObject, int amount,
                                                       IFluidTransfer sourceHandler, boolean doDrain) {
            return null;
        }
    };

    /**
     * @return the amount filled.
     */
    default long insertToHandler(@NotNull FluidTestObject testObject, long amount, @NotNull IFluidTransfer destHandler,
                                 boolean doFill) {
        return destHandler.fill(testObject.recombine(amount), doFill);
    }

    /**
     * @return the fluidstack drained.
     */
    @Nullable
    default FluidStack extractFromHandler(@Nullable FluidTestObject testObject, int amount,
                                          IFluidTransfer sourceHandler,
                                          boolean doDrain) {
        if (testObject == null) return sourceHandler.drain(amount, doDrain);
        else {
            FluidStack recombined = testObject.recombine();
            FluidStack drained = sourceHandler.drain(recombined, false);
            if (testObject.test(drained)) {
                if (doDrain) {
                    return sourceHandler.drain(recombined, true);
                } else {
                    return drained;
                }
            } else return null;
        }
    }
}
