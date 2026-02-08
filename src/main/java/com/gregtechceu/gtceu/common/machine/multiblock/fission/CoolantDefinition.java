package com.gregtechceu.gtceu.common.machine.multiblock.fission;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.world.level.material.Fluid;

import lombok.Getter;

@Getter
public class CoolantDefinition {

    private final Material coldMaterial;
    private final Material hotMaterial;
    private final int heatCapacity;
    private final int effectiveUpToTemp;
    private final float minEfficiency;

    public CoolantDefinition(Material coldMaterial, Material hotMaterial, int heatCapacity,
                             int effectiveUpToTemp, float minEfficiency) {
        this.coldMaterial = coldMaterial;
        this.hotMaterial = hotMaterial;
        this.heatCapacity = heatCapacity;
        this.effectiveUpToTemp = effectiveUpToTemp;
        this.minEfficiency = minEfficiency;
    }

    public Fluid getColdFluid() {
        return coldMaterial.getFluid();
    }

    public Fluid getHotFluid() {
        return hotMaterial.getFluid();
    }

    public float getEfficiency(int componentHeat, int componentMaxHeat) {
        if (componentHeat <= effectiveUpToTemp) return 1.0f;
        if (componentMaxHeat <= effectiveUpToTemp) return 1.0f;
        float progress = (float) (componentHeat - effectiveUpToTemp) / (componentMaxHeat - effectiveUpToTemp);
        return Math.max(minEfficiency, 1.0f - (1.0f - minEfficiency) * progress);
    }
}
