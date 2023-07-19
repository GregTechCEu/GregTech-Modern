package com.gregtechceu.gtceu.api.machine.trait.optical;

import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;

public interface IOpticalDataAccessHatch extends IDataAccessHatch {

    /**
     * @return if this hatch transmits data through cables
     */
    boolean isTransmitter();
}
