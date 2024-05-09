package com.gregtechceu.gtceu.api.material.material.event;


import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Event to modify and perform post-processing on materials
 * <br>
 * Material events are fired on the MOD bus as the forge bus isn't active until all mods have loaded.
 */
public class PostMaterialEvent extends Event implements IModBusEvent {

    public PostMaterialEvent() {
        super();
    }
}
