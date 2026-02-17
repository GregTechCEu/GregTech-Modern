package com.gregtechceu.gtceu.common.machine.multiblock.fission;

import com.gregtechceu.gtceu.common.data.GTMaterialItems;

import net.minecraft.world.item.Item;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class FuelRodRegistry {

    private static final Map<Item, FuelRodDefinition> REGISTRY = new HashMap<>();
    private static boolean initialized = false;

    public static void register(Item fuelItem, Item depletedItem, int lifetimeTicks,
                                int baseHeatGeneration, float endOfLifeHeatMultiplier) {
        FuelRodDefinition def = new FuelRodDefinition(
                fuelItem, depletedItem, lifetimeTicks, baseHeatGeneration, endOfLifeHeatMultiplier);
        REGISTRY.put(fuelItem, def);
    }

    @Nullable
    public static FuelRodDefinition getFuelDefinition(Item item) {
        ensureInitialized();
        return REGISTRY.get(item);
    }

    public static boolean isFuelRod(Item item) {
        ensureInitialized();
        return REGISTRY.containsKey(item);
    }

    private static void ensureInitialized() {
        if (!initialized) {
            init();
            initialized = true;
        }
    }

    private static void init() {
        register(GTMaterialItems.FUEL_ROD_LEU.asItem(), GTMaterialItems.DEPLETED_ROD_LEU.asItem(),
                72000, 300, 1.5f);
        register(GTMaterialItems.FUEL_ROD_MOX.asItem(), GTMaterialItems.DEPLETED_ROD_MOX.asItem(),
                36000, 800, 2.0f);
        register(GTMaterialItems.FUEL_ROD_THORIUM.asItem(), GTMaterialItems.DEPLETED_ROD_THORIUM.asItem(),
                144000, 300, 1.2f);
    }
}
