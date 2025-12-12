package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;

import java.util.function.Function;

public class MachineRenderState extends StateHolder<MachineDefinition, MachineRenderState> {

    /**
     * CODEC that always returns interned (canonical) state instances.
     * This is critical because StateHolder uses identity-based lookups internally.
     */
    public static final Codec<MachineRenderState> CODEC = codec(GTRegistries.MACHINES.byNameCodec(),
            MachineDefinition::defaultRenderState)
            .xmap(MachineRenderState::intern, Function.identity())
            .stable();

    /**
     * Returns the interned (canonical) instance of this state from the definition's StateDefinition.
     * This ensures identity-based lookups work correctly.
     */
    public MachineRenderState intern() {
        for (MachineRenderState interned : getDefinition().getStateDefinition().getPossibleStates()) {
            if (interned.getValues().equals(this.getValues())) {
                return interned;
            }
        }
        return this; // Fallback if not found (shouldn't happen)
    }

    public MachineRenderState(MachineDefinition owner, Reference2ObjectArrayMap<Property<?>, Comparable<?>> values,
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
