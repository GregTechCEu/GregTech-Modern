package com.gregtechceu.gtceu.api.capability;

import java.math.BigInteger;

public interface IEnergyInfoProvider {

    record EnergyInfo(BigInteger capacity, BigInteger stored) {}

    EnergyInfo getEnergyInfo();

    long getInputPerSec();

    long getOutputPerSec();

    boolean supportsBigIntEnergyValues();

    /**
     * @return true if information like energy capacity should be hidden from TOP.
     *         Useful for cables
     */
    default boolean isOneProbeHidden() {
        return false;
    }
}
