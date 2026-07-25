package com.gregtechceu.gtceu.common.data.loot;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.loot.serializer.CodecBasedSerializer;
import com.gregtechceu.gtceu.common.loot.condition.GTConfigValueCondition;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class GTLootConditions {
    // spotless:off

    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, GTCEu.MOD_ID);


    public static final RegistryObject<LootItemConditionType> CONFIG_VALUE = LOOT_CONDITION_TYPES.register("config_value", () -> new LootItemConditionType(new CodecBasedSerializer<>(GTConfigValueCondition.CODEC)));

    // spotless:on

    public static void init(IEventBus modBus) {
        LOOT_CONDITION_TYPES.register(modBus);
    }
}
