package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.GTValues;

public interface IOverclockMachine extends IMachineFeature {

    void setOverclockTier(int tier);

    long getOverclockVoltage();
}
