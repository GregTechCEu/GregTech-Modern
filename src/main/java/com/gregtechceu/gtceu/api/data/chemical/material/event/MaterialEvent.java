package com.gregtechceu.gtceu.api.data.chemical.material.event;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.IModBusEvent;

/**
 * @deprecated Your mod content classes should be loaded on startup instead, in your mod's main init files (e.g. {@link com.gregtechceu.gtceu.common.CommonProxy#init(IEventBus)}
 */
@Deprecated(forRemoval = true, since = "8.0.0")
public class MaterialEvent extends Event implements IModBusEvent {

    public MaterialEvent() {
        super();
    }
}
