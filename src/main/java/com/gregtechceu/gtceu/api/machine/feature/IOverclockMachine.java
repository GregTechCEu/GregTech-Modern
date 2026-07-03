package com.gregtechceu.gtceu.api.machine.feature;

public interface IOverclockMachine extends IMachineFeature {

    void setOverclockTier(int tier);

    long getOverclockVoltage();
}
