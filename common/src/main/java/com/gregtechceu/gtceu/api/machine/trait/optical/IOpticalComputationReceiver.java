package com.gregtechceu.gtceu.api.machine.trait.optical;

import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;

/**
 * Used in conjunction with {@link }.
 */
public interface IOpticalComputationReceiver extends IMachineFeature {

    IOpticalComputationProvider getComputationProvider();
}
