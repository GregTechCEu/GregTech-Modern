package com.gregtechceu.gtceu.api.capability;

import java.math.BigInteger;

public interface IEnergyInfoProvider {

    record EnergyInfo(BigInteger capacity, BigInteger stored) {}

    EnergyInfo getEnergyInfo();

    boolean supportsBigIntEnergyValues();
}
