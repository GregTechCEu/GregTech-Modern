package com.lowdragmc.gtceu.api.machine.feature;

import com.lowdragmc.gtceu.api.machine.MetaMachine;

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
