package com.gregtechceu.gtceu.common.machine.multiblock.fission;

import net.minecraft.nbt.CompoundTag;

import lombok.Getter;
import lombok.Setter;

@Getter
public class ReactorComponent {

    private final ReactorComponentType type;
    private final int maxHeat;
    @Setter
    private int baseHeatGeneration;
    private final int baseCoolingRate;
    @Setter
    private boolean active;
    private int heat;
    @Setter
    private int insertionDepth;
    @Setter
    private int ticksAlive;
    @Setter
    private int maxLifetimeTicks;
    @Setter
    private float endOfLifeMultiplier;

    public ReactorComponent(ReactorComponentType type, int maxHeat, int baseHeatGeneration, int baseCoolingRate) {
        this.type = type;
        this.maxHeat = maxHeat;
        this.baseHeatGeneration = baseHeatGeneration;
        this.baseCoolingRate = baseCoolingRate;
        this.active = true;
        this.endOfLifeMultiplier = 1.0f;
    }

    public void addHeat(int amount) {
        heat = Math.min(heat + amount, Integer.MAX_VALUE);
    }

    public void removeHeat(int amount) {
        heat = Math.max(heat - amount, 0);
    }

    public void setHeat(int value) {
        heat = Math.max(0, value);
    }

    public float heatPercent() {
        return maxHeat > 0 ? (float) heat / maxHeat : 0;
    }

    public int getEffectiveHeatGeneration() {
        if (type != ReactorComponentType.FUEL_ROD || maxLifetimeTicks <= 0) {
            return baseHeatGeneration;
        }
        int threshold = (int) (maxLifetimeTicks * 0.8f);
        if (ticksAlive <= threshold) {
            return baseHeatGeneration;
        }
        float progress = (float) (ticksAlive - threshold) / (maxLifetimeTicks - threshold);
        float multiplier = 1.0f + (endOfLifeMultiplier - 1.0f) * Math.min(progress, 1.0f);
        return (int) (baseHeatGeneration * multiplier);
    }

    public boolean isDepleted() {
        return type == ReactorComponentType.FUEL_ROD && maxLifetimeTicks > 0 && ticksAlive >= maxLifetimeTicks;
    }

    public void tickFuelAge() {
        if (type == ReactorComponentType.FUEL_ROD && active && maxLifetimeTicks > 0) {
            ticksAlive++;
        }
    }

    public CompoundTag serializeToNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("type", type.ordinal());
        tag.putInt("maxHeat", maxHeat);
        tag.putInt("heatGen", baseHeatGeneration);
        tag.putInt("coolRate", baseCoolingRate);
        tag.putBoolean("active", active);
        tag.putInt("heat", heat);
        tag.putInt("insertionDepth", insertionDepth);
        tag.putInt("ticksAlive", ticksAlive);
        tag.putInt("maxLifetime", maxLifetimeTicks);
        tag.putFloat("eolMultiplier", endOfLifeMultiplier);
        return tag;
    }

    public static ReactorComponent deserializeFromNbt(CompoundTag tag) {
        ReactorComponentType type = ReactorComponentType.values()[tag.getInt("type")];
        ReactorComponent comp = new ReactorComponent(
                type, tag.getInt("maxHeat"), tag.getInt("heatGen"), tag.getInt("coolRate"));
        comp.setActive(tag.getBoolean("active"));
        comp.setHeat(tag.getInt("heat"));
        comp.setInsertionDepth(tag.getInt("insertionDepth"));
        comp.setTicksAlive(tag.getInt("ticksAlive"));
        comp.setMaxLifetimeTicks(tag.getInt("maxLifetime"));
        comp.setEndOfLifeMultiplier(tag.getFloat("eolMultiplier"));
        return comp;
    }

    public static ReactorComponent fuelRod(int maxHeat, int heatGen) {
        return new ReactorComponent(ReactorComponentType.FUEL_ROD, maxHeat, heatGen, 0);
    }

    public static ReactorComponent coolantChannel(int maxHeat, int coolingRate) {
        return new ReactorComponent(ReactorComponentType.COOLANT_CHANNEL, maxHeat, 0, coolingRate);
    }

    public static ReactorComponent heatExchanger(int maxHeat) {
        return new ReactorComponent(ReactorComponentType.HEAT_EXCHANGER, maxHeat, 0, 0);
    }

    public static ReactorComponent neutronReflector(int maxHeat) {
        return new ReactorComponent(ReactorComponentType.NEUTRON_REFLECTOR, maxHeat, 0, 0);
    }

    public static ReactorComponent moderator(int maxHeat) {
        return new ReactorComponent(ReactorComponentType.MODERATOR, maxHeat, 0, 0);
    }

    public static ReactorComponent controlRod(int maxHeat) {
        return new ReactorComponent(ReactorComponentType.CONTROL_ROD, maxHeat, 0, 0);
    }
}
