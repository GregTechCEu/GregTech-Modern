package com.gregtechceu.gtceu.data.command;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.command.argument.MedicalConditionArgument;

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

    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister
            .create(Registries.COMMAND_ARGUMENT_TYPE, GTCEu.MOD_ID);

    private static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<MedicalConditionArgument>> MEDICAL_CONDITION_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES
            .register("medical_condition",
                    () -> ArgumentTypeInfos.registerByClass(MedicalConditionArgument.class,
                            SingletonArgumentInfo.contextFree(MedicalConditionArgument::medicalCondition)));

    @SuppressWarnings({ "SameParameterValue", "unchecked" })
    private static <T extends ArgumentType<?>> Class<T> fixClassType(Class<? super T> type) {
        return (Class<T>) type;
    }

    public static void init(IEventBus modBus) {
        COMMAND_ARGUMENT_TYPES.register(modBus);
    }
}
