package com.gregtechceu.gtceu.client.model.machine.multipart;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;

import net.minecraft.world.level.block.state.StateDefinition;

import com.google.common.collect.Streams;

import java.util.List;
import java.util.function.Predicate;

public class OrPartCondition implements PartCondition {

    public static final String TOKEN = "OR";
    private final Iterable<? extends PartCondition> conditions;

    public OrPartCondition(Iterable<? extends PartCondition> conditions) {
        this.conditions = conditions;
    }

    public Predicate<MachineRenderState> getPredicate(StateDefinition<MachineDefinition, MachineRenderState> def) {
        List<Predicate<MachineRenderState>> predicates = Streams.stream(this.conditions)
                .map((condition) -> condition.getPredicate(def))
                .toList();
        return (state) -> predicates.stream().anyMatch((p) -> p.test(state));
    }
}
