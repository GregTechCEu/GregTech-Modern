package com.gregtechceu.gtceu.data.pack.event;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import org.jetbrains.annotations.ApiStatus;

public class RegisterDynamicResourcesEvent extends Event implements IModBusEvent {

    @ApiStatus.Internal
    public RegisterDynamicResourcesEvent() {}
}
