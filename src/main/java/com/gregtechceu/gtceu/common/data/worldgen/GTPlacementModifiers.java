package com.gregtechceu.gtceu.common.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.worldgen.modifier.BiomeDependentPlacement;
import com.gregtechceu.gtceu.common.worldgen.modifier.RubberTreeChancePlacement;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class GTPlacementModifiers {
    // spotless:off

    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, GTCEu.MOD_ID);


    public static final RegistryObject<PlacementModifierType<BiomeDependentPlacement>> BIOME_DEPENDENT = PLACEMENT_MODIFIERS.register("biome_dependent", () -> () -> BiomeDependentPlacement.CODEC);
    public static final RegistryObject<PlacementModifierType<RubberTreeChancePlacement>> RUBBER_TREE_CHANCE = PLACEMENT_MODIFIERS.register("rubber_tree_chance", () -> () -> RubberTreeChancePlacement.CODEC);

    // spotless:on

    public static void init(IEventBus modBus) {
        PLACEMENT_MODIFIERS.register(modBus);
    }
}
