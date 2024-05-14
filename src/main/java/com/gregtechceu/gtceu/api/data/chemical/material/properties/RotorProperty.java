package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import org.jetbrains.annotations.NotNull;

public class RotorProperty implements IMaterialProperty<RotorProperty> {

    /**
     * Speed of rotors made from this Material.
     * <p>
     * Default:
     */
    private float speed;

    /**
     * Attack damage of rotors made from this Material
     * <p>
     * Default:
     */
    private float damage;

    /**
     * Durability of rotors made from this Material.
     * <p>
     * Default:
     */
    private int durability;
    /**
     * Efficiency of rotors made from this Material
     * <p>
     * Default:
     */
    private float efficiency;

    public RotorProperty(float speed, float efficiency, float damage, int durability) {
        this.speed = speed;
        this.efficiency = efficiency;
        this.damage = damage;
        this.durability = durability;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        if (speed <= 0) throw new IllegalArgumentException("Rotor Speed must be greater than zero!");
        this.speed = speed;
    }
    public float getEfficiency() {
        return efficiency;
    }
    public void setEfficiency(float efficiency) {
        if (efficiency <= 0) throw new IllegalArgumentException("Rotor Efficiency must be greater than zero!");
        this.efficiency = efficiency;
    }
    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        if (damage <= 0) throw new IllegalArgumentException("Rotor Attack Damage must be greater than zero!");
        this.damage = damage;
    }

    public int getDurability() {
        return durability;
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
