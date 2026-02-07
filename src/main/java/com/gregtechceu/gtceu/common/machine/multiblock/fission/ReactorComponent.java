package com.gregtechceu.gtceu.common.machine.multiblock.fission;

import net.minecraft.nbt.CompoundTag;

import lombok.Getter;
import lombok.Setter;

@Getter
public class ReactorComponent {

    private final ReactorComponentType type;
    private final int maxHeat;
    private final int baseHeatGeneration;
    private final int baseCoolingRate;
    @Setter
    private boolean active;
    private int heat;

    public ReactorComponent(ReactorComponentType type, int maxHeat, int baseHeatGeneration, int baseCoolingRate) {
        this.type = type;
        this.maxHeat = maxHeat;
        this.baseHeatGeneration = baseHeatGeneration;
        this.baseCoolingRate = baseCoolingRate;
        this.active = true;
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

    public CompoundTag serializeToNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("type", type.ordinal());
        tag.putInt("maxHeat", maxHeat);
        tag.putInt("heatGen", baseHeatGeneration);
        tag.putInt("coolRate", baseCoolingRate);
        tag.putBoolean("active", active);
        tag.putInt("heat", heat);
        return tag;
    }

    public static ReactorComponent deserializeFromNbt(CompoundTag tag) {
        ReactorComponentType type = ReactorComponentType.values()[tag.getInt("type")];
        ReactorComponent comp = new ReactorComponent(
                type, tag.getInt("maxHeat"), tag.getInt("heatGen"), tag.getInt("coolRate"));
        comp.setActive(tag.getBoolean("active"));
        comp.setHeat(tag.getInt("heat"));
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
