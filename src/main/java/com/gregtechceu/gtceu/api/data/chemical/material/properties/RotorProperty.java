package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class RotorProperty implements IMaterialProperty<RotorProperty> {

    /**
     * Power of rotors made from this Material.
     * <p>
     * Default:
     */
    @Getter
    private float power;

    /**
     * Attack damage of rotors made from this Material
     * <p>
     * Default:
     */
    @Getter
    private float damage;

    /**
     * Durability of rotors made from this Material.
     * <p>
     * Default:
     */
    @Getter
    private int durability;
    /**
     * Efficiency of rotors made from this Material
     * <p>
     * Default:
     */
    @Getter
    private float efficiency;

    public RotorProperty(float power, float efficiency, float damage, int durability) {
        this.power = power;
        this.efficiency = efficiency;
        this.damage = damage;
        this.durability = durability;
    }

    public void setPower(float power) {
        if (power <= 0) throw new IllegalArgumentException("Rotor Power must be greater than zero!");
        this.power = power;
    }

    public void setEfficiency(float efficiency) {
        if (efficiency <= 0) throw new IllegalArgumentException("Rotor Efficiency must be greater than zero!");
        this.efficiency = efficiency;
    }

    public void setDamage(float damage) {
        if (damage <= 0) throw new IllegalArgumentException("Rotor Attack Damage must be greater than zero!");
        this.damage = damage;
    }

    public void setDurability(int durability) {
        if (durability <= 0) throw new IllegalArgumentException("Rotor Durability must be greater than zero!");
        this.durability = durability;
    }

    @Override
    public void verifyProperty(@NotNull MaterialProperties properties) {
        properties.ensureSet(PropertyKey.INGOT, true);
    }
}
