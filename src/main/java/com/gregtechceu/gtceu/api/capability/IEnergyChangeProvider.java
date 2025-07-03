package com.gregtechceu.gtceu.api.capability;

public interface IEnergyChangeProvider {

    record EnergyChange(long averageInLastSec, long averageOutLastSec) {}

    EnergyChange getEnergyChange();
}
