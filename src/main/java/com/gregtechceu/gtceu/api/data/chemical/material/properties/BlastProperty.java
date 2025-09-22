package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import lombok.Getter;
import lombok.Setter;

public class BlastProperty implements IMaterialProperty {

    /**
     * Blast Furnace Temperature of this Material.
     * If below 1000K, Primitive Blast Furnace recipes will be also added.
     * If above 1750K, a Hot Ingot and its Vacuum Freezer recipe will be also added.
     * <p>
     * If a Material with this Property has a Fluid, its temperature
     * will be set to this if it is the default Fluid temperature.
     */
    @Getter
    private int blastTemperature;

    /**
     * The {@link GasTier} of this Material, representing which Gas EBF recipes will be generated.
     * <p>
     * Default: null, meaning no Gas EBF recipes.
     */
    @Setter
    @Getter
    private GasTier gasTier = null;

    /**
     * The duration of the EBF recipe, overriding the stock behavior.
     * <p>
     * Default: -1, meaning the duration will be: material.getAverageMass() * blastTemperature / 50
     */
    @Setter
    @Getter
    private int durationOverride = -1;

    /**
     * The EU/t of the EBF recipe, overriding the stock behavior.
     * <p>
     * Default: -1, meaning the EU/t will be 120.
     */
    @Setter
    @Getter
    private int EUtOverride = -1;

    /**
     * The duration of the EBF recipe, overriding the stock behavior.
     * <p>
     * Default: -1, meaning the duration will be: material.getMass() * 3
     */
    @Setter
    @Getter
    private int vacuumDurationOverride = -1;

    /**
     * The EU/t of the Vacuum Freezer recipe (if needed), overriding the stock behavior.
     * <p>
     * Default: -1, meaning the EU/t will be 120 EU/t.
     */
    @Setter
    @Getter
    private int vacuumEUtOverride = -1;

    public BlastProperty(int blastTemperature) {
        this.blastTemperature = blastTemperature;
    }

    public BlastProperty(int blastTemperature, GasTier gasTier) {
        this.blastTemperature = blastTemperature;
        this.gasTier = gasTier;
    }

    public BlastProperty(int blastTemperature, GasTier gasTier, int eutOverride, int durationOverride,
                         int vacuumEUtOverride, int vacuumDurationOverride) {
        this.blastTemperature = blastTemperature;
        this.gasTier = gasTier;
        this.EUtOverride = eutOverride;
        this.durationOverride = durationOverride;
        this.vacuumEUtOverride = vacuumEUtOverride;
        this.vacuumDurationOverride = vacuumDurationOverride;
    }

    /**
     * Default property constructor.
     */
    public BlastProperty() {
        this(0);
    }

    public void setBlastTemperature(int blastTemp) {
        if (blastTemp <= 0) throw new IllegalArgumentException("Blast Temperature must be greater than zero!");
        this.blastTemperature = blastTemp;
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
            throw new IllegalArgumentException(
                    "Could not find valid gas tier for name: " + gasTierName + ". " + message);
        }
    }

    public enum GasTier {

        // Tiers used by GTCEu
        LOW(() -> FluidIngredient.of(GTMaterials.Nitrogen.getFluidTag(), 1000)),
        MID(() -> FluidIngredient.of(GTMaterials.Helium.getFluidTag(), 100)),
        HIGH(() -> FluidIngredient.of(GTMaterials.Argon.getFluidTag(), 50)),

        // Tiers reserved for addons
        HIGHER(() -> FluidIngredient.of(GTMaterials.Neon.getFluidTag(), 25)),
        HIGHEST(() -> FluidIngredient.of(GTMaterials.Krypton.getFluidTag(), 10));

        public static final GasTier[] VALUES = values();
        private Supplier<FluidIngredient> fluid;

        GasTier(Supplier<FluidIngredient> fluid) {
            this.fluid = Suppliers.memoize(fluid);
        }

        public void setFluid(Supplier<FluidIngredient> fluid) {
            this.fluid = Suppliers.memoize(fluid);
        }

        public FluidIngredient getFluid() {
            return fluid.get().copy();
        }
    }

    @SuppressWarnings("unused") // API, need to treat all of these as used
    public static class Builder {

        private int temp;
        private GasTier gasTier;
        private int eutOverride = -1;
        private int durationOverride = -1;
        private int vacuumEUtOverride = -1;
        private int vacuumDurationOverride = -1;

        public Builder() {}

        /**
         * Set the EBF temperature of this Material.
         * <br>
         * <br>
         * If the temperature is above <strong>1750K</strong>, it will automatically add a Vacuum Freezer recipe and Hot
         * Ingot.<br>
         * If the temperature is below <strong>1000K</strong>, it will automatically add a PBF recipe in addition to the
         * EBF recipe.
         *
         * @param temperature The temperature of the recipe in the EBF.
         */
        public Builder temp(int temperature) {
            this.temp = temperature;
            return this;
        }

        /**
         * Set the EBF temperature and gas tier of this Material.
         * <br>
         * <br>
         * If the temperature is above <strong>1750K</strong>, it will automatically add a Vacuum Freezer recipe and Hot
         * Ingot.<br>
         * If the temperature is below <strong>1000K</strong>, it will automatically add a PBF recipe in addition to the
         * EBF recipe.
         *
         * @param temperature The temperature of the recipe in the EBF.
         * @param gasTier     The {@link GasTier} of the Recipe. Will generate a second EBF recipe
         *                    using the specified gas of the tier for a speed bonus.
         */
        public Builder temp(int temperature, GasTier gasTier) {
            this.temp = temperature;
            this.gasTier = gasTier;
            return this;
        }

        /**
         * Set the EU/t of the EBF recipe for this Material.
         */
        public Builder blastStats(int eutOverride) {
            this.eutOverride = eutOverride;
            return this;
        }

        /**
         * Set the EU/t and duration of the EBF recipe for this Material.
         */
        public Builder blastStats(int eutOverride, int durationOverride) {
            this.eutOverride = eutOverride;
            this.durationOverride = durationOverride;
            return this;
        }

        /**
         * Set the EU/t of the Vacuum Freezer recipe for the Hot Ingot of this Material.
         */
        public Builder vacuumStats(int eutOverride) {
            this.vacuumEUtOverride = eutOverride;
            return this;
        }

        /**
         * Set the EU/t and duration of the Vacuum Freezer recipe for the Hot Ingot of this Material.
         */
        public Builder vacuumStats(int eutOverride, int durationOverride) {
            this.vacuumEUtOverride = eutOverride;
            this.vacuumDurationOverride = durationOverride;
            return this;
        }

        public BlastProperty build() {
            return new BlastProperty(temp, gasTier, eutOverride, durationOverride, vacuumEUtOverride,
                    vacuumDurationOverride);
        }
    }
}
