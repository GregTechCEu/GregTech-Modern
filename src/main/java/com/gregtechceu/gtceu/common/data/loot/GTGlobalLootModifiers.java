package com.gregtechceu.gtceu.common.data.loot;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.loot.modifier.ApplyHardHammerEnchantmentModifier;

import com.mojang.serialization.MapCodec;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class GTGlobalLootModifiers {
    // spotless:off

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, GTCEu.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<ApplyHardHammerEnchantmentModifier>> APPLY_HARD_HAMMER_ENCHANT = GLOBAL_LOOT_MODIFIER_SERIALIZERS.register("apply_hard_hammer_enchant", () -> ApplyHardHammerEnchantmentModifier.CODEC);

    // spotless:on

    public static void init(IEventBus modBus) {
        GLOBAL_LOOT_MODIFIER_SERIALIZERS.register(modBus);
    }
}
