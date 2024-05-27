package com.gregtechceu.gtceu.api.data.chemical.material.event;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

/**
 * Event to register and modify materials in
 * <br>
 * Material events are fired on the MOD bus as the forge bus isn't active until all mods have loaded.
 */
public class MaterialEvent extends Event implements IModBusEvent {

    public MaterialEvent() {
        super();
    }
}
