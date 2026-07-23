package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.loot.ChestGenHooks;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class GTLootItemFunctions {

    private static final DeferredRegister<LootItemFunctionType> LOOT_ITEM_FUNCTION_TYPE = DeferredRegister
            .create(Registries.LOOT_FUNCTION_TYPE, GTCEu.MOD_ID);

    public static final RegistryObject<LootItemFunctionType> RANDOM_WEIGHT_LOOT_FUNCTION_TYPE = LOOT_ITEM_FUNCTION_TYPE
            .register("random_weight",
                    () -> new LootItemFunctionType(new ChestGenHooks.RandomWeightLootFunction.Serializer()));

    public static void init(IEventBus modBus) {
        LOOT_ITEM_FUNCTION_TYPE.register(modBus);
    }
}
