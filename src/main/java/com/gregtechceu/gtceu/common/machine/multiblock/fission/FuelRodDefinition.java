package com.gregtechceu.gtceu.common.machine.multiblock.fission;

import net.minecraft.world.item.Item;

import lombok.Getter;

@Getter
public class FuelRodDefinition {

    private final Item fuelItem;
    private final Item depletedItem;
    private final int totalLifetimeTicks;
    private final int baseHeatGeneration;
    private final float endOfLifeHeatMultiplier;

    public FuelRodDefinition(Item fuelItem, Item depletedItem, int totalLifetimeTicks,
                             int baseHeatGeneration, float endOfLifeHeatMultiplier) {
        this.fuelItem = fuelItem;
        this.depletedItem = depletedItem;
        this.totalLifetimeTicks = totalLifetimeTicks;
        this.baseHeatGeneration = baseHeatGeneration;
        this.endOfLifeHeatMultiplier = endOfLifeHeatMultiplier;
    }
}
