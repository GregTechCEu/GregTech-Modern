package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.util.ExtraCodecs;

public class DustProperty implements IMaterialProperty<DustProperty> {
    public static final Codec<DustProperty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ExtraCodecs.POSITIVE_INT.optionalFieldOf("harvest_level", 2).forGetter(val -> val.harvestLevel),
        ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("burn_time", 0).forGetter(val -> val.burnTime)
    ).apply(instance, DustProperty::new));

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
