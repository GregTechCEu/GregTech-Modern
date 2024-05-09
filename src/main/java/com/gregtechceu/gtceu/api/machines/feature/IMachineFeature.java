package com.gregtechceu.gtceu.api.machines.feature;

import com.gregtechceu.gtceu.api.machines.MetaMachine;

/**
 * @author KilaBash
 * @date 2023/2/18
 * @implNote IMachine
 */
public interface IMachineFeature {
    default MetaMachine self() {
        return (MetaMachine) this;
    }

}
