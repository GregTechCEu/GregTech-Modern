package com.gregtechceu.gtceu.api.events;

import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import lombok.Getter;

public class ModifyMachineEvent extends Event implements IModBusEvent {

    @Getter
    private final MachineBuilder<?, ?, ?> builder;

    public ModifyMachineEvent(MachineBuilder<?, ?, ?> builder) {
        this.builder = builder;
    }
}
