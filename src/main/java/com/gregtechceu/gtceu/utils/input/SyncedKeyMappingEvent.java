package com.gregtechceu.gtceu.utils.input;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Event to register {@link SyncedKeyMapping}s in.
 * <br>
 * Event is fired on the mod bus.
 */
public class SyncedKeyMappingEvent extends Event implements IModBusEvent {

    public SyncedKeyMappingEvent() {
        super();
    }
}
