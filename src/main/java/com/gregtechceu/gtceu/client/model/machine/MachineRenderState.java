package com.gregtechceu.gtceu.client.model.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public class MachineRenderState extends StateHolder<MachineDefinition, MachineRenderState> {

    public static final Codec<MachineRenderState> CODEC = codec(GTRegistries.MACHINES.codec(),
            MachineDefinition::defaultRenderState).stable();

    public MachineRenderState(MachineDefinition owner, ImmutableMap<Property<?>, Comparable<?>> values,
                              MapCodec<MachineRenderState> propertiesCodec) {
        super(owner, values, propertiesCodec);
    }

    public MachineDefinition getDefinition() {
        return this.owner;
    }

    public boolean is(MetaMachine machine) {
        return this.is(machine.getDefinition());
    }

    public boolean is(MachineDefinition definition) {
        return this.getDefinition() == definition;
    }
}
