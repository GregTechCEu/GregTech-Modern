package com.gregtechceu.gtceu.common.machine.owner;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import java.util.UUID;
import java.util.function.Function;

/**
 * Event to register/override the machine ownership object generator.
 * This event isn't made with addons/other mods in mind and in most cases should be avoided!!!
 * Its intended use is to enable mod-pack creators to change how the ownership system works.
 */
public class RegisterOwnerTypeEvent extends Event implements IModBusEvent {

    int priority = -1;
    Function<UUID, MachineOwner> ownershipProvider;

    /**
     * @param priority           the providers priority value (highest priority is selected).
     *                           <p>
     *                           The default ownership generator is assigned priority 0.
     * @param ownershipGenerator the ownership object generator
     */
    public void register(int priority, Function<UUID, MachineOwner> ownershipGenerator) {
        if (priority > this.priority) {
            this.priority = priority;
            this.ownershipProvider = ownershipGenerator;
        }
    }
}
