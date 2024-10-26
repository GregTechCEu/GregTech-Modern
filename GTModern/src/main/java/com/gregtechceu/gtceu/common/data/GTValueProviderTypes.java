package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.valueprovider.AddedFloat;
import com.gregtechceu.gtceu.common.valueprovider.CastedFloat;
import com.gregtechceu.gtceu.common.valueprovider.FlooredInt;
import com.gregtechceu.gtceu.common.valueprovider.MultipliedFloat;

import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.FloatProviderType;
import net.minecraft.util.valueproviders.IntProviderType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class GTValueProviderTypes {

    public static final DeferredRegister<IntProviderType<?>> INT_PROVIDER_TYPE_REGISTER = DeferredRegister.create(
            Registries.INT_PROVIDER_TYPE,
            GTCEu.MOD_ID);
    public static final DeferredRegister<FloatProviderType<?>> FLOAT_PROVIDER_TYPE_REGISTER = DeferredRegister.create(
            Registries.FLOAT_PROVIDER_TYPE,
            GTCEu.MOD_ID);

    public static final RegistryObject<IntProviderType<FlooredInt>> FLOORED = INT_PROVIDER_TYPE_REGISTER.register(
            "floored",
            () -> () -> FlooredInt.CODEC);

    public static final RegistryObject<FloatProviderType<MultipliedFloat>> MULTIPLIED = FLOAT_PROVIDER_TYPE_REGISTER
            .register("multiplied",
                    () -> () -> MultipliedFloat.CODEC);
    public static final RegistryObject<FloatProviderType<AddedFloat>> ADDED = FLOAT_PROVIDER_TYPE_REGISTER.register(
            "added",
            () -> () -> AddedFloat.CODEC);
    public static final RegistryObject<FloatProviderType<CastedFloat>> CASTED = FLOAT_PROVIDER_TYPE_REGISTER.register(
            "casted",
            () -> () -> CastedFloat.CODEC);

    public static void init(IEventBus bus) {
        INT_PROVIDER_TYPE_REGISTER.register(bus);
        FLOAT_PROVIDER_TYPE_REGISTER.register(bus);
    }
}
