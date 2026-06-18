package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;

public class InfiniteEnergyContainer extends NotifiableEnergyContainer {

    public InfiniteEnergyContainer(long maxCapacity, long maxInputVoltage, long maxInputAmperage,
                                   long maxOutputVoltage, long maxOutputAmperage) {
        super(maxCapacity, maxInputVoltage, maxInputAmperage, maxOutputVoltage, maxOutputAmperage);
    }

    @Override
    public long getEnergyStored() {
        return getEnergyCapacity();
    }
}
