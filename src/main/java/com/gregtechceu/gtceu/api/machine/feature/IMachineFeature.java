package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

public interface IMachineFeature {

    default MetaMachine self() {
        return (MetaMachine) this;
    }
}
