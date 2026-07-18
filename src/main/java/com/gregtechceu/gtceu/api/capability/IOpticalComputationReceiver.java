package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableComputationContainer;

/**
 * Used in conjunction with {@link NotifiableComputationContainer}.
 */
public interface IOpticalComputationReceiver {

    IOpticalComputationProvider getComputationProvider();
}
