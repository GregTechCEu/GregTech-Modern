package com.gregtechceu.gtceu.api.graphnet.traverse;

public class DistributorHelper {

    private final long maximum;
    private long consumption;

    public DistributorHelper(long maximum, long consumption) {
        this.maximum = maximum;
        this.consumption = consumption;
    }

    public void addConsumption(long consumption) {
        this.consumption += consumption;
    }

    public boolean supportsMult(long mult) {
        return this.maximum >= this.consumption * mult;
    }

    public long withMult(long mult) {
        return this.consumption * mult;
    }
}
