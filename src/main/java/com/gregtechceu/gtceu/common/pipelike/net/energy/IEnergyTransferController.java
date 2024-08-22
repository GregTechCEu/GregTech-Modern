package com.gregtechceu.gtceu.common.pipelike.net.energy;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.graphnet.pipenet.transfer.TransferControl;
import com.gregtechceu.gtceu.api.graphnet.pipenet.transfer.TransferControlProvider;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IEnergyTransferController {

    TransferControl<IEnergyTransferController> CONTROL = new TransferControl<>("Energy") {

        @Override
        public @NotNull IEnergyTransferController get(@Nullable Object potentialHolder) {
            if (!(potentialHolder instanceof TransferControlProvider holder)) return DEFAULT;
            IEnergyTransferController found = holder.getControllerForControl(CONTROL);
            return found == null ? DEFAULT : found;
        }

        @Override
        public @NotNull IEnergyTransferController getNoPassage() {
            return NO_PASSAGE;
        }
    };

    IEnergyTransferController DEFAULT = new IEnergyTransferController() {};

    IEnergyTransferController NO_PASSAGE = new IEnergyTransferController() {

        @Override
        public long insertToHandler(long voltage, long amperage, @NotNull IEnergyContainer destHandler,
                                    Direction side, boolean simulate) {
            return 0;
        }
    };

    /**
     * @return inserted amperes
     */
    default long insertToHandler(long voltage, long amperage, @NotNull IEnergyContainer destHandler, Direction side,
                                 boolean simulate) {
        return destHandler.acceptEnergyFromNetwork(side, voltage, amperage, simulate);
    }
}
