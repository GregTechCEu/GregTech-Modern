package com.gregtechceu.gtceu.integration.jsonthings.serializers;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.integration.jsonthings.builders.MachineBuilder;

import net.minecraft.resources.ResourceLocation;

public interface IMachineFactory<D extends MachineDefinition, M extends MetaMachine> {

    D construct(ResourceLocation id, MachineBuilder builder);

    M create(IMachineBlockEntity holder, D definition);
}
