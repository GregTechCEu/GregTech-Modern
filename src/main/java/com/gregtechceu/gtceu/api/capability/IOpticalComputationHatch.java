package com.gregtechceu.gtceu.api.capability;

public interface IOpticalComputationHatch extends IOpticalComputationProvider {

    /** If this hatch transmits or receives CWU/t. */
    boolean isTransmitter();
}
