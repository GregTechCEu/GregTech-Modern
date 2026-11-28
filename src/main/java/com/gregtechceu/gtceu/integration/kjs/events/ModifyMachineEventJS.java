package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.events.ContentModificationEvent;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.event.EventJS;

import java.util.function.Consumer;

public class ModifyMachineEventJS extends EventJS {

    private final ContentModificationEvent.Machine event;

    public ModifyMachineEventJS(ContentModificationEvent.Machine event) {
        this.event = event;
    }

    public ResourceLocation getId() {
        return this.event.getId();
    }

    public MachineBuilder<?, ?, ?> getBuilder() {
        return this.event.getBuilder();
    }

    public void afterCreation(Consumer<? extends MetaMachine> creationCallback) {
        this.event.afterCreation(creationCallback);
    }
}
