package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.modifier.BiomePlacement;
import com.gregtechceu.gtceu.api.data.worldgen.modifier.DimensionFilter;
import com.gregtechceu.gtceu.api.data.worldgen.modifier.FrequencyModifier;
import com.gregtechceu.gtceu.common.worldgen.modifier.RubberTreeChancePlacement;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GTPlacementModifierTypes {

    private static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegister
            .create(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, GTCEu.MOD_ID);

    // spotless:off
    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<DimensionFilter>> DIMENSION_FILTER =
            PLACEMENT_MODIFIERS.register("dimension", () -> () -> DimensionFilter.CODEC);

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<FrequencyModifier>> FREQUENCY_MODIFIER =
            PLACEMENT_MODIFIERS.register("frequency", () -> () -> FrequencyModifier.CODEC);

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<RubberTreeChancePlacement>> RUBBER_TREE_CHANCE_PLACEMENT =
            PLACEMENT_MODIFIERS.register("rubber_tree_chance", () -> () -> RubberTreeChancePlacement.CODEC);

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<BiomePlacement>> BIOME_PLACEMENT =
            PLACEMENT_MODIFIERS.register("biome_placement", () -> () -> BiomePlacement.CODEC);

    //spotless:on

    public static void init(IEventBus modBus) {
        PLACEMENT_MODIFIERS.register(modBus);
    }
}
