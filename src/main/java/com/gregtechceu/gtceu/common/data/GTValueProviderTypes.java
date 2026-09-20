package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.valueprovider.AddedFloat;
import com.gregtechceu.gtceu.common.valueprovider.CastedFloat;
import com.gregtechceu.gtceu.common.valueprovider.FlooredInt;
import com.gregtechceu.gtceu.common.valueprovider.MultipliedFloat;

import net.minecraft.core.registries.Registries;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class GTValueProviderTypes {

    // spotless:off
    public static final DeferredRegister<MapCodec<? extends IntProvider>> INT_PROVIDER_TYPE_REGISTER = DeferredRegister.create(Registries.INT_PROVIDER_TYPE, GTCEu.MOD_ID);
    public static final DeferredRegister<MapCodec<? extends FloatProvider>> FLOAT_PROVIDER_TYPE_REGISTER = DeferredRegister.create(Registries.FLOAT_PROVIDER_TYPE, GTCEu.MOD_ID);


    public static final DeferredHolder<MapCodec<? extends IntProvider>, MapCodec<FlooredInt>> FLOORED = INT_PROVIDER_TYPE_REGISTER.register("floored", () -> FlooredInt.CODEC);

    public static final DeferredHolder<MapCodec<? extends FloatProvider>, MapCodec<MultipliedFloat>> MULTIPLIED = FLOAT_PROVIDER_TYPE_REGISTER.register("multiplied", () -> MultipliedFloat.CODEC);
    public static final DeferredHolder<MapCodec<? extends FloatProvider>, MapCodec<AddedFloat>> ADDED = FLOAT_PROVIDER_TYPE_REGISTER.register("added", () -> AddedFloat.CODEC);
    public static final DeferredHolder<MapCodec<? extends FloatProvider>, MapCodec<CastedFloat>> CASTED = FLOAT_PROVIDER_TYPE_REGISTER.register("casted", () -> CastedFloat.CODEC);

    // spotless:on

    public static void init(IEventBus bus) {
        INT_PROVIDER_TYPE_REGISTER.register(bus);
        FLOAT_PROVIDER_TYPE_REGISTER.register(bus);
    }
}
