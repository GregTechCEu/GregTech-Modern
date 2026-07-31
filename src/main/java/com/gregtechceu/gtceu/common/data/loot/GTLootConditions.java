package com.gregtechceu.gtceu.common.data.loot;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.loot.condition.GTConfigValueCondition;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GTLootConditions {
    // spotless:off

    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, GTCEu.MOD_ID);


    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> CONFIG_VALUE = LOOT_CONDITION_TYPES.register("config_value", () -> new LootItemConditionType(GTConfigValueCondition.CODEC));

    // spotless:on

    public static void init(IEventBus modBus) {
        LOOT_CONDITION_TYPES.register(modBus);
    }
}
