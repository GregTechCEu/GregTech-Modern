package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.UtilsJS;
import dev.latvian.mods.rhino.BaseFunction;

import java.util.Locale;
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
        CustomTieredMachineBuilder[] builders = new CustomTieredMachineBuilder[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            CustomTieredMachineBuilder register = new CustomTieredMachineBuilder(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name, holder -> machineConstructor.apply(holder, tier)).tier(tier);
            builders[tier] = register;
        }
        return builders;
    }

    @SuppressWarnings("unchecked")
    public static MachineBuilder<MachineDefinition> createAll(String name, Object... args) {
        CustomTieredMachineBuilder[] builders = new CustomTieredMachineBuilder[0];
        if (args.length > 1) {
            Integer[] tiers = MachineFunctionPresets.mapTierArray(MachineFunctionPresets.copyArgs(args, 1));
            if (args[0] instanceof BiFunction<?,?,?> machineFunction) {
                builders = customTiered(name, (BiFunction<IMachineBlockEntity, Integer, MetaMachine>) machineFunction, tiers);
            } else if (args[0] instanceof BaseFunction machineFunction) {
                //builders = customTiered(name, (BiFunction<IMachineBlockEntity, Integer, MetaMachine>) NativeJavaObject.createInterfaceAdapter(ScriptType.STARTUP.manager.get().context, BiFunction.class, machineFunction), MachineFunctionPresets.mapTierArray(tiers));
                builders = customTiered(name, UtilsJS.makeFunctionProxy(ScriptType.STARTUP, BiFunction.class, machineFunction), tiers);
            }
        }
        return MachineFunctionPresets.builder(name, builders, CustomTieredMachineBuilder.class, MachineDefinition::createDefinition, MetaMachineBlock::new, MetaMachineBlockEntity::createBlockEntity);
    }
}
