package com.gregtechceu.gtceu.api.capability.impl;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.ILaserContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class ActiveTransformerWrapper implements ILaserContainer {

    @Nullable
    private final IEnergyContainer energyInputs;
    @Nullable
    private final ILaserContainer laserUpstream;

    public ActiveTransformerWrapper(@Nullable IEnergyContainer energyInputs, @Nullable ILaserContainer laserUpstream) {
        this.energyInputs = energyInputs;
        this.laserUpstream = laserUpstream;
    }

    @Override
    public long changeEnergy(long amount, @Nonnull Collection<ILaserContainer> seen) {
        seen.add(this);
        long used = 0;
        if (energyInputs != null) {
            used = energyInputs.changeEnergy(amount);
        }
        if (Math.abs(used) < Math.abs(amount) && laserUpstream != null && !seen.contains(laserUpstream)) {
            used += laserUpstream.changeEnergy(amount - used);
        }
        return used;
    }

    @Override
    public long getEnergyStored(@Nonnull Collection<ILaserContainer> seen) {
        seen.add(this);
        long stored = 0;
        if (energyInputs != null) {
            stored = energyInputs.getEnergyStored();
        }
        if (laserUpstream != null && !seen.contains(laserUpstream)) {
            stored += laserUpstream.getEnergyStored(seen);
        }
        return stored;
    }

    @Override
    public long getEnergyCapacity(@Nonnull Collection<ILaserContainer> seen) {
        seen.add(this);
        long capacity = 0;
        if (energyInputs != null) {
            capacity = energyInputs.getEnergyCapacity();
        }
        if (laserUpstream != null && !seen.contains(laserUpstream)) {
            capacity += laserUpstream.getEnergyCapacity(seen);
        }
        return capacity;
    }
}
