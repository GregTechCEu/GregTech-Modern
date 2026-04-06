package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.utils.GTMath;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_FOIL;

public class WireProperties implements IMaterialProperty {

    private long voltage;
    private int amperage;
    private int lossPerBlock;
    private int superconductorCriticalTemperature;
    private boolean isSuperconductor;

    public WireProperties(long voltage, int baseAmperage, int lossPerBlock) {
        this(voltage, baseAmperage, lossPerBlock, false);
    }

    public WireProperties(long voltage, int baseAmperage, int lossPerBlock, boolean isSuperCon) {
        this(voltage, baseAmperage, lossPerBlock, isSuperCon, 0);
    }

    public WireProperties(long voltage, int baseAmperage, int lossPerBlock, boolean isSuperCon,
                          int criticalTemperature) {
        this.voltage = voltage;
        this.amperage = baseAmperage;
        this.lossPerBlock = isSuperCon ? 0 : lossPerBlock;
        this.superconductorCriticalTemperature = isSuperCon ? criticalTemperature : 0;
        this.isSuperconductor = isSuperCon;
    }

    public WireProperties copy() {
        return new WireProperties(voltage, amperage, lossPerBlock, isSuperconductor, superconductorCriticalTemperature);
    }

    /**
     * Default values constructor
     */
    public WireProperties() {
        this(8, 1, 1, false);
    }

    /**
     * Retrieves the current wire voltage
     *
     * @return The current wire voltage
     */
    public long getVoltage() {
        return voltage;
    }

    /**
     * Sets the current wire voltage
     *
     * @param voltage The new wire voltage
     */
    public void setVoltage(long voltage) {
        this.voltage = voltage;
    }

    /**
     * Retrieves the current wire amperage
     *
     * @return The current wire amperage
     */
    public int getAmperage() {
        return amperage;
    }

    /**
     * Sets the current wire amperage
     *
     * @param amperage The new current wire amperage
     */
    public void setAmperage(int amperage) {
        this.amperage = amperage;
    }

    /**
     * Retrieves the current wire loss per block
     *
     * @return The current wire loss per block
     */
    public int getLossPerBlock() {
        return lossPerBlock;
    }

    /**
     * Sets the current wire loss per block
     *
     * @param lossPerBlock The new wire loss per block
     */
    public void setLossPerBlock(int lossPerBlock) {
        this.lossPerBlock = lossPerBlock;
    }

    /**
     * If the current wire is a Superconductor wire
     *
     * @return {@code true} if the current wire is a Superconductor
     */
    public boolean isSuperconductor() {
        return isSuperconductor;
    }

    /**
     * Sets the current wire to a superconductor wire
     *
     * @param isSuperconductor The new wire superconductor status
     */
    public void setSuperconductor(boolean isSuperconductor) {
        this.isSuperconductor = isSuperconductor;
    }

    /**
     * Retrieves the critical temperature of the superconductor (the temperature at which the superconductive phase
     * transition happens)
     *
     * @return Critical temperature of the material
     */
    public int getSuperconductorCriticalTemperature() {
        return superconductorCriticalTemperature;
    }

    /**
     * Sets the material's critical temperature
     *
     * @param criticalTemperature The new critical temperature
     */
    public void setSuperconductorCriticalTemperature(int criticalTemperature) {
        this.superconductorCriticalTemperature = this.isSuperconductor ? criticalTemperature : 0;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
        if (properties.hasProperty(PropertyKey.INGOT)) {
            // Ensure all Materials with Cables and voltage tier IV or above have a Foil for recipe generation
            Material thisMaterial = properties.getMaterial();
            if (!isSuperconductor && voltage >= GTValues.V[GTValues.IV] && !thisMaterial.hasFlag(GENERATE_FOIL)) {
                thisMaterial.addFlags(GENERATE_FOIL);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WireProperties that)) return false;
        return voltage == that.voltage &&
                amperage == that.amperage &&
                lossPerBlock == that.lossPerBlock &&
                superconductorCriticalTemperature == that.superconductorCriticalTemperature &&
                isSuperconductor == that.isSuperconductor;
    }

    @Override
    public int hashCode() {
        return GTMath.hashLongs(voltage, amperage, lossPerBlock);
    }
}
