package com.gregtechceu.gtceu.api.events;

import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import lombok.Getter;

public class ModifyMachineEvent extends Event implements IModBusEvent {

    @Getter
    private final MachineBuilder<?, ?, ?> builder;

    public ModifyMachineEvent(MachineBuilder<?, ?, ?> builder) {
        this.builder = builder;
    }
}
