package com.gregtechceu.gtceu.api.misc;

import com.gregtechceu.gtceu.api.capability.IEnergyChangeProvider;

import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EnergyChangeProviderList implements IEnergyChangeProvider {

    private final List<? extends IEnergyChangeProvider> list;

    public EnergyChangeProviderList(List<? extends IEnergyChangeProvider> list) {
        this.list = list;
    }

    @Override
    public long getAverageInputLastSec() {
        long sum = 0;
        for (IEnergyChangeProvider ecp : list) {
            sum += ecp.getAverageInputLastSec();
        }
        return sum;
    }

    @Override
    public long getAverageOutputLastSec() {
        long sum = 0;
        for (IEnergyChangeProvider ecp : list) {
            sum += ecp.getAverageOutputLastSec();
        }
        return sum;
    }
}
