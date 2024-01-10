package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

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
