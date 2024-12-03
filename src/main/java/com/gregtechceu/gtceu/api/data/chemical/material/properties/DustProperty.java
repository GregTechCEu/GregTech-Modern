package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import lombok.Getter;

public class DustProperty implements IMaterialProperty {

    /**
     * Tool level needed to harvest block of this Material.
     * <p>
     * Default: 2 (Iron).
     */
    @Getter
    private int harvestLevel;

    /**
     * Burn time of this Material when used as fuel in Furnace smelting.
     * Zero or negative value indicates that this Material cannot be used as fuel.
     * <p>
     * Default: 0.
     */
    @Getter
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

    public void setBurnTime(int burnTime) {
        if (burnTime < 0) throw new IllegalArgumentException("Burn Time cannot be negative!");
        this.burnTime = burnTime;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {}
}
