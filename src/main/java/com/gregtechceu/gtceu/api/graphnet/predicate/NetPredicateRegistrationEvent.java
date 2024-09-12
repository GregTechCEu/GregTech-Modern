package com.gregtechceu.gtceu.api.graphnet.predicate;

import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import java.util.Comparator;

public final class NetPredicateRegistrationEvent extends Event implements IModBusEvent {

    private final ObjectRBTreeSet<NetPredicateType<?>> gather = new ObjectRBTreeSet<>(
            Comparator.comparing(NetPredicateType::getSerializedName));

    public void accept(NetPredicateType<?> type) {
        if (!gather.add(type))
            throw new IllegalStateException("Detected a name collision during Net Predicate registration!");
    }

    ObjectRBTreeSet<NetPredicateType<?>> getGather() {
        return gather;
    }
}
