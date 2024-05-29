package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;

public class InfiniteEnergyContainer extends NotifiableEnergyContainer {

    public InfiniteEnergyContainer(MetaMachine machine, long maxCapacity, long maxInputVoltage, long maxInputAmperage,
                                   long maxOutputVoltage, long maxOutputAmperage) {
        super(machine, maxCapacity, maxInputVoltage, maxInputAmperage, maxOutputVoltage, maxOutputAmperage);
    }

    @Override
    public long getEnergyStored() {
        return getEnergyCapacity();
    }
}
