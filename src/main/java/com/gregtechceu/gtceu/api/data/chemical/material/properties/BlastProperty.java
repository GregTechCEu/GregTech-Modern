package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import javax.annotation.Nonnull;

public class BlastProperty implements IMaterialProperty<BlastProperty> {

    /**
     * Blast Furnace Temperature of this Material.
     * If below 1000K, Primitive Blast Furnace recipes will be also added.
     * If above 1750K, a Hot Ingot and its Vacuum Freezer recipe will be also added.
     * <p>
     * If a Material with this Property has a Fluid, its temperature
     * will be set to this if it is the default Fluid temperature.
     */
    private int blastTemperature;

    /**
     * The {@link GasTier} of this Material, representing which Gas EBF recipes will be generated.
     * <p>
     * Default: null, meaning no Gas EBF recipes.
     */
    private GasTier gasTier = null;

    /**
     * The duration of the EBF recipe, overriding the stock behavior.
     * <p>
     * Default: -1, meaning the duration will be: material.getAverageMass() * blastTemperature / 50
     */
    private int durationOverride = -1;

    /**
     * The EU/t of the EBF recipe, overriding the stock behavior.
     * <p>
     * Default: -1, meaning the EU/t will be 120.
     */
    private int eutOverride = -1;

    public BlastProperty(int blastTemperature) {
        this.blastTemperature = blastTemperature;
    }

    public BlastProperty(int blastTemperature, GasTier gasTier, int eutOverride, int durationOverride) {
        this.blastTemperature = blastTemperature;
        this.gasTier = gasTier;
        this.eutOverride = eutOverride;
        this.durationOverride = durationOverride;
    }

    /**
     * Default property constructor.
     */
    public BlastProperty() {
        this(0);
    }

    public int getBlastTemperature() {
        return blastTemperature;
    }

    public void setBlastTemperature(int blastTemp) {
        if (blastTemp <= 0) throw new IllegalArgumentException("Blast Temperature must be greater than zero!");
        this.blastTemperature = blastTemp;
    }

    public GasTier getGasTier() {
        return gasTier;
    }

    public void setGasTier(@Nonnull GasTier tier) {
        this.gasTier = tier;
    }

    public int getDurationOverride() {
        return durationOverride;
    }

    public void setDurationOverride(int duration) {
        this.durationOverride = duration;
    }

    public int getEUtOverride() {
        return eutOverride;
    }

    public void setEutOverride(int eut) {
        this.eutOverride = eut;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.INGOT, true);
    }

    public static GasTier validateGasTier(String gasTierName) {
        if (gasTierName == null) return null;
        else if ("LOW".equalsIgnoreCase(gasTierName)) return GasTier.LOW;
        else if ("MID".equalsIgnoreCase(gasTierName)) return GasTier.MID;
        else if ("HIGH".equalsIgnoreCase(gasTierName)) return GasTier.HIGH;
        else if ("HIGHER".equalsIgnoreCase(gasTierName)) return GasTier.HIGHER;
        else if ("HIGHEST".equalsIgnoreCase(gasTierName)) return GasTier.HIGHEST;
        else {
            String message = "Gas Tier must be either \"LOW\", \"MID\", \"HIGH\", \"HIGHER\", or \"HIGHEST\"";
            throw new IllegalArgumentException("Could not find valid gas tier for name: " + gasTierName + ". " + message);
        }
    }

    public enum GasTier {
        // Tiers used by GTCEu
        LOW, MID, HIGH,

        // Tiers reserved for addons
        HIGHER, HIGHEST;

        public static final GasTier[] VALUES = values();
    }
}
