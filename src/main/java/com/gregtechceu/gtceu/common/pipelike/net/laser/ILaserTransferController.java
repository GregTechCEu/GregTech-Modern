package com.gregtechceu.gtceu.common.pipelike.net.laser;

import com.gregtechceu.gtceu.api.capability.ILaserRelay;
import com.gregtechceu.gtceu.api.graphnet.pipenet.transfer.TransferControl;
import com.gregtechceu.gtceu.api.graphnet.pipenet.transfer.TransferControlProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ILaserTransferController {

    TransferControl<ILaserTransferController> CONTROL = new TransferControl<>("Laser") {

        @Override
        public @NotNull ILaserTransferController get(@Nullable Object potentialHolder) {
            if (!(potentialHolder instanceof TransferControlProvider holder)) return DEFAULT;
            ILaserTransferController found = holder.getControllerForControl(CONTROL);
            return found == null ? DEFAULT : found;
        }

        @Override
        public @NotNull ILaserTransferController getNoPassage() {
            return NO_PASSAGE;
        }
    };

    ILaserTransferController DEFAULT = new ILaserTransferController() {};

    ILaserTransferController NO_PASSAGE = new ILaserTransferController() {

        @Override
        public long insertToHandler(long voltage, long amperage, @NotNull ILaserRelay destHandler) {
            return 0;
        }
    };

    /**
     * @return inserted amperes
     */
    default long insertToHandler(long voltage, long amperage, @NotNull ILaserRelay destHandler) {
        return destHandler.receiveLaser(voltage, amperage);
    }
}
