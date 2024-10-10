package com.gregtechceu.gtceu.common.pipelike.net.energy;

public record EnergyFlowData(long amperage, long voltage) {

    public long getEU() {
        return amperage * voltage;
    }
}
