package com.gregtechceu.gtceu.common.machine.multiblock.fission;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.level.material.Fluid;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CoolantRegistry {

    private static final Map<Fluid, CoolantDefinition> REGISTRY = new HashMap<>();
    private static boolean initialized = false;

    public static void register(Material cold, Material hot, int heatCapacity,
                                int effectiveUpToTemp, float minEfficiency) {
        CoolantDefinition def = new CoolantDefinition(cold, hot, heatCapacity, effectiveUpToTemp, minEfficiency);
        REGISTRY.put(cold.getFluid(), def);
    }

    @Nullable
    public static CoolantDefinition getCoolant(Fluid fluid) {
        ensureInitialized();
        return REGISTRY.get(fluid);
    }

    public static boolean isCoolant(Fluid fluid) {
        ensureInitialized();
        return REGISTRY.containsKey(fluid);
    }

    private static void ensureInitialized() {
        if (!initialized) {
            init();
            initialized = true;
        }
    }

    private static void init() {
        register(GTMaterials.Water, GTMaterials.Steam, 10, 1000, 0.3f);
        register(GTMaterials.DistilledWater, GTMaterials.Steam, 15, 1500, 0.4f);
        register(GTMaterials.SodiumPotassium, GTMaterials.HotSodiumPotassium, 25, 2500, 0.5f);
    }
}
