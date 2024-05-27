package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.commands.arguments.HazardEffectsArgument;
import com.gregtechceu.gtceu.common.commands.arguments.MaterialArgument;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class GTCommandArguments {

    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister
            .create(Registries.COMMAND_ARGUMENT_TYPE, GTCEu.MOD_ID);

    private static final RegistryObject<SingletonArgumentInfo<MaterialArgument>> MATERIAL_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES
            .register(
                    "material", () -> ArgumentTypeInfos.registerByClass(MaterialArgument.class,
                            SingletonArgumentInfo.contextFree(MaterialArgument::material)));
    private static final RegistryObject<SingletonArgumentInfo<HazardEffectsArgument>> HAZARD_EFFECTS_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES
            .register(
                    "hazard_effects", () -> ArgumentTypeInfos.registerByClass(HazardEffectsArgument.class,
                            SingletonArgumentInfo.contextFree(HazardEffectsArgument::new)));

    public static void init(IEventBus modBus) {
        COMMAND_ARGUMENT_TYPES.register(modBus);
    }
}
