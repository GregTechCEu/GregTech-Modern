package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class RotorProperty implements IMaterialProperty {

    /**
     * Power of rotors made from this Material.
     * <p>
     * Default:
     */
    @Getter
    private int power;

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
    private int efficiency;

    public RotorProperty(int power, int efficiency, float damage, int durability) {
        this.power = power;
        this.efficiency = efficiency;
        this.damage = damage;
        this.durability = durability;
    }

    public void setPower(int power) {
        if (power <= 0) throw new IllegalArgumentException("Rotor Power must be greater than zero!");
        this.power = power;
    }

    public void setEfficiency(int efficiency) {
        if (efficiency <= 0) throw new IllegalArgumentException("Rotor Efficiency must be greater than zero!");
        this.efficiency = efficiency;
    }

    public void setDamage(int damage) {
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
