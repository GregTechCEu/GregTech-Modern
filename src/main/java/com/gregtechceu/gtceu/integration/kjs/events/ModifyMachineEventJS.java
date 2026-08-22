package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.events.ModifyMachineEvent;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;

import dev.latvian.mods.kubejs.event.KubeEvent;

public class ModifyMachineEventJS implements KubeEvent {

    private final ModifyMachineEvent event;

    public ModifyMachineEventJS(ModifyMachineEvent event) {
        this.event = event;
    }

    public MachineBuilder<?, ?, ?> getBuilder() {
        return this.event.getBuilder();
    }
}
