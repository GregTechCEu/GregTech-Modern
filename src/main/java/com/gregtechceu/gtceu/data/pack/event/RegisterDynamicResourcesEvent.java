package com.gregtechceu.gtceu.data.pack.event;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import org.jetbrains.annotations.ApiStatus;

public class RegisterDynamicResourcesEvent extends Event implements IModBusEvent {

    @ApiStatus.Internal
    public RegisterDynamicResourcesEvent() {}
}
