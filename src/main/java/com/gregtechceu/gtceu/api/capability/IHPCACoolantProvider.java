package com.gregtechceu.gtceu.api.capability;

public interface IHPCACoolantProvider extends IHPCAComponentHatch {

    /**
     * How much this part cools down the HPCA per tick.
     */
    int getCoolingAmount();

    /**
     * Whether this HPCA Coolant Provider is active (requires coolant) or passive (free).
     */
    boolean isActiveCooler();

    /**
     * How much coolant to use while HPCA is running.
     * Actual amount used will depend on how much of this cooler is actually being utilized
     * by Computation providers.
     *
     * @return The amount of coolant to use per tick, in mB/t
     */
    default int getMaxCoolantPerTick() {
        return 0;
    }
}
