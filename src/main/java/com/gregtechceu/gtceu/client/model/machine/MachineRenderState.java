package com.gregtechceu.gtceu.client.model.machine;

import com.google.common.collect.ImmutableMap;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;

public class MachineRenderState extends StateHolder<MachineDefinition, MachineRenderState> {

    public MachineRenderState(MachineDefinition owner, ImmutableMap<Property<?>, Comparable<?>> values, MapCodec<MachineRenderState> propertiesCodec) {
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
