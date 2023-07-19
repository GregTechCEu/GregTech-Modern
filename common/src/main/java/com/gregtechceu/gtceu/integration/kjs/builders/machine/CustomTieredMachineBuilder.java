package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;

import java.util.function.BiFunction;
import java.util.function.Function;

public class CustomTieredMachineBuilder extends SimpleMachineBuilder {
    public CustomTieredMachineBuilder(String name, Function<IMachineBlockEntity, MetaMachine> machineConstructor) {
        super(name, machineConstructor);
    }

    @Override
    public CustomTieredMachineBuilder tier(int tier) {
        return (CustomTieredMachineBuilder) super.tier(tier);
    }

    private static CustomTieredMachineBuilder[] customTiered(String name,
                                                       BiFunction<IMachineBlockEntity, Integer, MetaMachine> machineConstructor,
                                                       Integer... tiers) {
        CustomTieredMachineBuilder[] builders = new CustomTieredMachineBuilder[tiers.length];
        for (int tier : tiers) {
            CustomTieredMachineBuilder register = new CustomTieredMachineBuilder(GTValues.VN[tier].toLowerCase() + "_" + name, holder -> machineConstructor.apply(holder, tier)).tier(tier);
            builders[tier] = register;
        }
        return builders;
    }

    @SuppressWarnings("unchecked")
    public static MachineBuilder<MachineDefinition> createAll(String name, Object... args) {
        CustomTieredMachineBuilder[] builders = new CustomTieredMachineBuilder[0];
        if (args.length > 1 && args[1] instanceof BiFunction<?,?,?> machineFunction) {
            Object[] tiers = MachineFunctionPresets.copyArgs(args, 1);
            builders = customTiered(name, (BiFunction<IMachineBlockEntity, Integer, MetaMachine>) machineFunction, MachineFunctionPresets.mapTierArray(tiers));
        }
        return MachineFunctionPresets.builder(name, builders, CustomTieredMachineBuilder.class, MachineDefinition::createDefinition, MetaMachineBlock::new, MetaMachineBlockEntity::createBlockEntity);
    }
}
