package com.gregtechceu.gtceu.common.data.loot;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.loot.modifier.AddTableLootModifier;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class GTGlobalLootModifiers {
    // spotless:off

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, GTCEu.MOD_ID);


    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ADD_TABLE_LOOT_MODIFIER = GLOBAL_LOOT_MODIFIER_SERIALIZERS.register("add_table", () -> AddTableLootModifier.CODEC);

    // spotless:on


    public static void init(IEventBus modBus) {
        GLOBAL_LOOT_MODIFIER_SERIALIZERS.register(modBus);
    }
}
