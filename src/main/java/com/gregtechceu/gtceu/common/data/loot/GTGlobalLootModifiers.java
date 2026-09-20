package com.gregtechceu.gtceu.common.data.loot;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.loot.modifier.AddTableLootModifier;

import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

import com.mojang.serialization.Codec;

public class GTGlobalLootModifiers {
    // spotless:off

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, GTCEu.MOD_ID);


    public static final DeferredHolder<Codec<? extends IGlobalLootModifier>, Codec<? extends IGlobalLootModifier>> ADD_TABLE_LOOT_MODIFIER = GLOBAL_LOOT_MODIFIER_SERIALIZERS.register("add_table", () -> AddTableLootModifier.CODEC);

    // spotless:on

    public static void init(IEventBus modBus) {
        GLOBAL_LOOT_MODIFIER_SERIALIZERS.register(modBus);
    }
}
