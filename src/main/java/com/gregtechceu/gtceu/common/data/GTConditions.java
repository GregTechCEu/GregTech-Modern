package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.condition.IsDevEnvironmentCondition;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class GTConditions {

    public static void init(IEventBus modBus) {
        CONDITION_CODECS.register(modBus);
    }

    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, GTCEu.MOD_ID);

    public static final Supplier<MapCodec<IsDevEnvironmentCondition>> IS_DEV_ENV =
            CONDITION_CODECS.register("is_dev_env", () -> IsDevEnvironmentCondition.CODEC);

}
