package com.gregtechceu.gtceu.api.graphnet.logic;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;

import java.util.Comparator;

public final class NetLogicRegistrationEvent extends Event implements IModBusEvent {

    private final ObjectRBTreeSet<NetLogicType<?>> gather = new ObjectRBTreeSet<>(
            Comparator.comparing(NetLogicType::getSerializedName));

    public void accept(NetLogicType<?> type) {
        if (!gather.add(type))
            throw new IllegalStateException("Detected a name collision during Net Logic registration!");
    }

    ObjectRBTreeSet<NetLogicType<?>> getGather() {
        return gather;
    }
}
