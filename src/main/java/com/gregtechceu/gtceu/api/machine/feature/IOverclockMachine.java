package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.GTValues;

public interface IOverclockMachine extends IMachineFeature {

    int getOverclockTier();

    void setOverclockTier(int tier);

    int getMaxOverclockTier();

    int getMinOverclockTier();

    default long getOverclockVoltage() {
        return GTValues.V[getOverclockTier()];
    }
}
