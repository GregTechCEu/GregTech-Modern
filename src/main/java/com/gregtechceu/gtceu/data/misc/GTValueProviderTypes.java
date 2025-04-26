package com.gregtechceu.gtceu.data.misc;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.valueprovider.AddedFloat;
import com.gregtechceu.gtceu.common.valueprovider.CastedFloat;
import com.gregtechceu.gtceu.common.valueprovider.FlooredInt;
import com.gregtechceu.gtceu.common.valueprovider.MultipliedFloat;

import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.FloatProviderType;
import net.minecraft.util.valueproviders.IntProviderType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GTValueProviderTypes {

    public static final DeferredRegister<IntProviderType<?>> INT_PROVIDER_TYPES = DeferredRegister
            .create(Registries.INT_PROVIDER_TYPE, GTCEu.MOD_ID);
    public static final DeferredRegister<FloatProviderType<?>> FLOAT_PROVIDER_TYPES = DeferredRegister
            .create(Registries.FLOAT_PROVIDER_TYPE, GTCEu.MOD_ID);

    public static final DeferredHolder<IntProviderType<?>, IntProviderType<FlooredInt>> FLOORED = INT_PROVIDER_TYPES
            .register(
                    "floored",
                    () -> () -> FlooredInt.CODEC);

    public static final DeferredHolder<FloatProviderType<?>, FloatProviderType<MultipliedFloat>> MULTIPLIED = FLOAT_PROVIDER_TYPES
            .register("multiplied",
                    () -> () -> MultipliedFloat.CODEC);
    public static final DeferredHolder<FloatProviderType<?>, FloatProviderType<AddedFloat>> ADDED = FLOAT_PROVIDER_TYPES
            .register(
                    "added",
                    () -> () -> AddedFloat.CODEC);
    public static final DeferredHolder<FloatProviderType<?>, FloatProviderType<CastedFloat>> CASTED = FLOAT_PROVIDER_TYPES
            .register(
                    "casted",
                    () -> () -> CastedFloat.CODEC);

    public static void register(IEventBus bus) {
        INT_PROVIDER_TYPES.register(bus);
        FLOAT_PROVIDER_TYPES.register(bus);
    }
}
