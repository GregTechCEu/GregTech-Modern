package com.gregtechceu.gtceu.api.data.worldgen.modifier;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.worldgen.modifier.RubberTreeChancePlacement;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GTPlacementModifiers {

    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER = DeferredRegister
            .create(Registries.PLACEMENT_MODIFIER_TYPE, GTCEu.MOD_ID);

    public static <V extends PlacementModifier> PlacementModifierType<V> register(String name,
                                                                                  PlacementModifierType<V> value) {
        PLACEMENT_MODIFIER.register(name, () -> (PlacementModifierType<?>) value);
        return value;
    }

    public static void init(IEventBus modBus) {
        PLACEMENT_MODIFIER.register(modBus);
        register("frequency", () -> FrequencyModifier.CODEC);
        register("dimension", () -> DimensionFilter.CODEC);
        register("biome_placement", () -> BiomePlacement.CODEC);
        register("rubber_tree_chance", () -> RubberTreeChancePlacement.CODEC);
    }
}
