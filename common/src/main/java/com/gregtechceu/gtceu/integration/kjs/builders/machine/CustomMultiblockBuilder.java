package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.api.machine.multiblock.TieredWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitiveFancyUIWorkableMachine;

import java.util.function.BiFunction;
import java.util.function.Function;

public class CustomMultiblockBuilder extends MultiblockMachineBuilder {
    protected CustomMultiblockBuilder(String name, Function<IMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine) {
        super(GTRegistries.REGISTRATE, name, metaMachine, MetaMachineBlock::new, MetaMachineItem::new, MetaMachineBlockEntity::createBlockEntity);
    }

    public static CustomMultiblockBuilder[] tieredMultis(String name,
                                                         BiFunction<IMachineBlockEntity, Integer, MultiblockControllerMachine> factory,
                                                         Integer... tiers) {
        CustomMultiblockBuilder[] builders = new CustomMultiblockBuilder[tiers.length];
        for (int i = 0; i < tiers.length; i++) {
            int tier = tiers[i];
            var builder = new CustomMultiblockBuilder(GTValues.VN[tier].toLowerCase() + "_" + name, holder -> factory.apply(holder, tier))
                    .tier(tier);
            builders[i] = builder;
        }
        return builders;
    }

    @Override
    public CustomMultiblockBuilder tier(int tier) {
        return (CustomMultiblockBuilder) super.tier(tier);
    }


    @SuppressWarnings("unchecked")
    public static MachineBuilder<MultiblockMachineDefinition> createMultiblock(String name, Object... args) {
        CustomMultiblockBuilder[] builders;
        int start = 0;
        while (start < args.length && (!(args[start] instanceof Number) || !(args[start] instanceof Number[]) || !(args[start] instanceof int[]))) {
            ++start;
        }
        Object[] tierObjects = MachineFunctionPresets.copyArgs(args, start);
        Integer[] tiers = MachineFunctionPresets.mapTierArray(tierObjects);
        if (tiers.length > 0) {
            if (args.length > 0 && args[0] instanceof BiFunction<?,?,?> machineFunction) {
                builders = tieredMultis(name, (BiFunction<IMachineBlockEntity, Integer, MultiblockControllerMachine>) machineFunction, tiers);
            } else {
                builders = tieredMultis(name, TieredWorkableElectricMultiblockMachine::new, tiers);
            }
        } else {
            if (args.length > 0 && args[0] instanceof Function<?,?> machineFunction) {
                return new CustomMultiblockBuilder(name, (Function<IMachineBlockEntity, MultiblockControllerMachine>)machineFunction);
            } else {
                return new CustomMultiblockBuilder(name, WorkableElectricMultiblockMachine::new);
            }
        }
        return MachineFunctionPresets.builder(name, builders, CustomMultiblockBuilder.class, MultiblockMachineDefinition::createDefinition, MetaMachineBlock::new, MetaMachineBlockEntity::createBlockEntity);
    }

    public static MachineBuilder<MultiblockMachineDefinition> createPrimitiveMultiblock(String name, Object... args) {
        return new CustomMultiblockBuilder(name, (holder) -> new PrimitiveFancyUIWorkableMachine(holder, args));
    }
}
