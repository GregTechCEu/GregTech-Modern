package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import javax.annotation.Nonnull;

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

    public RotorProperty(float speed, float damage, int durability) {
        this.speed = speed;
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
    public void verifyProperty(@Nonnull MaterialProperties properties) {
        properties.ensureSet(PropertyKey.INGOT, true);
    }
}
