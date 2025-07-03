package com.gregtechceu.gtceu.api.capability;

public interface IEnergyChangeProvider {

    long getAverageInputLastSec();

    long getAverageOutputLastSec();
}
