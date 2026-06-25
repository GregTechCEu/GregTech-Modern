package com.gregtechceu.gtceu.api.capability;

public interface IOpticalDataAccessHatch extends IDataAccessMachine {

    /**
     * @return if this hatch transmits data through cables
     */
    boolean isTransmitter();
}
