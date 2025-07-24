package com.gregtechceu.gtceu.api.misc;

import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;

import net.minecraft.MethodsReturnNonnullByDefault;

import java.math.BigInteger;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EnergyInfoProviderList implements IEnergyInfoProvider {

    private final List<? extends IEnergyInfoProvider> list;

    public EnergyInfoProviderList(List<? extends IEnergyInfoProvider> list) {
        this.list = list;
    }

    @Override
    public EnergyInfo getEnergyInfo() {
        BigInteger capacity = BigInteger.ZERO;
        BigInteger stored = BigInteger.ZERO;

        for (IEnergyInfoProvider energyInfoProvider : list) {
            EnergyInfo energyInfo = energyInfoProvider.getEnergyInfo();

            capacity = capacity.add(energyInfo.capacity());
            stored = stored.add(energyInfo.stored());
        }

        return new EnergyInfo(capacity, stored);
    }

    @Override
    public long getInputPerSec() {
        long sum = 0;
        for (IEnergyInfoProvider eip : list) {
            sum += eip.getInputPerSec();
        }
        return sum;
    }

    @Override
    public long getOutputPerSec() {
        long sum = 0;
        for (IEnergyInfoProvider eip : list) {
            sum += eip.getOutputPerSec();
        }
        return sum;
    }

    @Override
    public boolean supportsBigIntEnergyValues() {
        return list.size() > 1;
    }
}
