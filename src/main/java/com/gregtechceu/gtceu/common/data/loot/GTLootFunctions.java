package com.gregtechceu.gtceu.common.data.loot;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.loot.serializer.CodecBasedSerializer;
import com.gregtechceu.gtceu.common.loot.function.SetEUChargeFunction;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class GTLootFunctions {
    // spotless:off

    public static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTION_TYPES = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, GTCEu.MOD_ID);


    public static final RegistryObject<LootItemFunctionType> SET_EU_CHARGE = LOOT_FUNCTION_TYPES.register("set_eu_charge", () -> new LootItemFunctionType(new CodecBasedSerializer<>(SetEUChargeFunction.CODEC)));

    // spotless:on

    public static void init(IEventBus modBus) {
        LOOT_FUNCTION_TYPES.register(modBus);
    }
}
