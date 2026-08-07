package com.gregtechceu.gtceu.client;

import net.neoforged.bus.api.Event;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Fired when the corresponding GLFW event is triggered, before Minecraft's handling.
 * Cancelling this event will also cancel further handling by Minecraft, like firing the
 * {@link net.neoforged.neoforge.client.event.ScreenEvent.CharacterTyped} event.
 */
@Getter
public class CharTypedEvent extends Event {

    @Getter
    @Setter
    @Accessors(fluent = true)
    private boolean cancelled = false;
    private final char codepoint;
    private final int modifiers;

    public CharTypedEvent(char codepoint, int modifiers) {
        this.codepoint = codepoint;
        this.modifiers = modifiers;
    }
}
