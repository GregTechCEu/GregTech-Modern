package com.gregtechceu.gtceu.client.model.machine.multipart;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;

import net.minecraft.world.level.block.state.StateDefinition;

import java.util.function.Predicate;

@FunctionalInterface
public interface PartCondition {

    PartCondition TRUE = (definition) -> state -> true;
    PartCondition FALSE = (definition) -> state -> false;

    Predicate<MachineRenderState> getPredicate(StateDefinition<MachineDefinition, MachineRenderState> def);
}
