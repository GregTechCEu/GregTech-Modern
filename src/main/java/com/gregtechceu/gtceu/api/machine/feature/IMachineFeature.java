package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.blockentity.IGregtechBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

public interface IMachineFeature extends IGregtechBlockEntity {

    default MetaMachine self() {
        return (MetaMachine) this;
    }
}
