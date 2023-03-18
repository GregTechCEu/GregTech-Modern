package com.lowdragmc.gtceu.api.data.chemical.material.properties;

public class DustProperty implements IMaterialProperty<DustProperty> {

    /**
     * Tool level needed to harvest block of this Material.
     * <p>
     * Default: 2 (Iron).
     */
    private int harvestLevel;

    /**
     * Burn time of this Material when used as fuel in Furnace smelting.
     * Zero or negative value indicates that this Material cannot be used as fuel.
     * <p>
     * Default: 0.
     */
    private int burnTime;

    public DustProperty(int harvestLevel, int burnTime) {
        this.harvestLevel = harvestLevel;
        this.burnTime = burnTime;
    }

    /**
     * Default property constructor.
     */
    public DustProperty() {
        this(2, 0);
    }

    public void setHarvestLevel(int harvestLevel) {
        if (harvestLevel <= 0) throw new IllegalArgumentException("Harvest Level must be greater than zero!");
        this.harvestLevel = harvestLevel;
    }

    public int getHarvestLevel() {
        return this.harvestLevel;
    }

    public void setBurnTime(int burnTime) {
        if (burnTime < 0) throw new IllegalArgumentException("Burn Time cannot be negative!");
        this.burnTime = burnTime;
    }

    public int getBurnTime() {
        return burnTime;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        FluidProperty prop = properties.getProperty(PropertyKey.FLUID);
        if (prop != null && prop.getFluidTemperature() == FluidProperty.BASE_TEMP) {
            prop.setFluidTemperature(1200);
        }
    }
}
