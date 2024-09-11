package com.gregtechceu.gtceu.data.command;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistry;
import com.gregtechceu.gtceu.common.commands.arguments.GTRegistryArgument;
import com.gregtechceu.gtceu.common.commands.arguments.MaterialArgument;
import com.gregtechceu.gtceu.common.commands.arguments.MedicalConditionArgument;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.mojang.brigadier.arguments.ArgumentType;

@SuppressWarnings("unused")
public class GTCommandArguments {

    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister
            .create(Registries.COMMAND_ARGUMENT_TYPE, GTCEu.MOD_ID);

    private static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<MaterialArgument>> MATERIAL_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES
            .register(
                    "material", () -> ArgumentTypeInfos.registerByClass(MaterialArgument.class,
                            SingletonArgumentInfo.contextFree(MaterialArgument::material)));

    private static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<MedicalConditionArgument>> MEDICAL_CONDITION_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES
            .register("medical_condition",
                    () -> ArgumentTypeInfos.registerByClass(MedicalConditionArgument.class,
                            SingletonArgumentInfo.contextFree(MedicalConditionArgument::medicalCondition)));

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static final DeferredHolder<ArgumentTypeInfo<?, ?>, GTRegistryArgument.Info<?, ? extends GTRegistry<?, ?>>> GT_REGISTRY_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES
            .register("gt_registry",
                    () -> ArgumentTypeInfos.<GTRegistryArgument, GTRegistryArgument.Info.Template, GTRegistryArgument.Info>registerByClass(
                            fixClassType(GTRegistryArgument.class),
                            new GTRegistryArgument.Info<>()));

    @SuppressWarnings({ "SameParameterValue", "unchecked" })
    private static <T extends ArgumentType<?>> Class<T> fixClassType(Class<? super T> type) {
        return (Class<T>) type;
    }

    public static void init(IEventBus modBus) {
        COMMAND_ARGUMENT_TYPES.register(modBus);
    }
}
