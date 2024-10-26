package com.gregtechceu.gtceu.api.data.chemical.material.event;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

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
