package com.gregtechceu.gtceu.api.machine.trait.optical;

public interface IOpticalComputationHatch extends IOpticalComputationProvider {

    /** If this hatch transmits or receives CWU/t. */
    boolean isTransmitter();
}
