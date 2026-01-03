package com.gregtechceu.gtceu.api.capability;

/**
 * For the large gas turbine / large plasma turbine
 */
public interface ITurbineMachine extends IWorkable {

    /**
     * @return true if the machine has a roter installed
     */
    boolean hasRotor();

    /**
     * @return the current rotor speed or 0 when no rotor is installed
     */
    int getRotorSpeed();

    /**
     * @return the maximum rotor speed or 0 when no rotor is installed
     */
    int getMaxRotorHolderSpeed();

    /**
     * @return the total efficiency the rotor holder and rotor provide in % or -1 when no rotor is installed
     */
    int getTotalEfficiency();

    /**
     * @return the current energy production
     */
    long getCurrentProduction();

    /**
     * @return the maximum energy production
     */
    long getOverclockVoltage();

    /**
     * @return the rotor durability in % or -1 when no rotor is installed
     */
    int getRotorDurabilityPercent();
}
