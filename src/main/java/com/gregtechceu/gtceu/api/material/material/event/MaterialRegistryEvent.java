package com.gregtechceu.gtceu.api.material.material.event;

import com.gregtechceu.gtceu.api.material.material.IMaterialRegistryManager;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Event to add a material registry in.
 * <br>
 * Material events are fired on the MOD bus as the forge bus isn't active until all mods have loaded.
 *
 * @see IMaterialRegistryManager#createRegistry(String)
 */
public class MaterialRegistryEvent extends Event implements IModBusEvent {

    public MaterialRegistryEvent() {
        super();
    }
}
