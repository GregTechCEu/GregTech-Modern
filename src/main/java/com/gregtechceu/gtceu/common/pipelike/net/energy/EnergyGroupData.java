package com.gregtechceu.gtceu.common.pipelike.net.energy;

import com.gregtechceu.gtceu.api.graphnet.AbstractGroupData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnergyGroupData extends AbstractGroupData {

    private long lastEnergyInPerSec;
    private long lastEnergyOutPerSec;
    private long energyInPerSec;
    private long energyOutPerSec;
    private long updateTime;

    public long getEnergyInPerSec(long queryTick) {
        updateCache(queryTick);
        return lastEnergyInPerSec;
    }

    public long getEnergyOutPerSec(long queryTick) {
        updateCache(queryTick);
        return lastEnergyOutPerSec;
    }

    public void addEnergyInPerSec(long energy, long queryTick) {
        updateCache(queryTick);
        energyInPerSec += energy;
    }

    public void addEnergyOutPerSec(long energy, long queryTick) {
        updateCache(queryTick);
        energyOutPerSec += energy;
    }

    private void updateCache(long queryTick) {
        if (queryTick > updateTime) {
            updateTime = updateTime + 20;
            clearCache();
        }
    }

    public void clearCache() {
        lastEnergyInPerSec = energyInPerSec;
        lastEnergyOutPerSec = energyOutPerSec;
        energyInPerSec = 0;
        energyOutPerSec = 0;
    }

    @Override
    public boolean mergeAllowed(@NotNull AbstractGroupData other) {
        return true;
    }

    @Override
    public @Nullable AbstractGroupData merge(@NotNull AbstractGroupData other) {
        if (other instanceof EnergyGroupData)
            return new EnergyGroupData();
        else return null;
    }
}
