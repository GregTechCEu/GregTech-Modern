package com.gregtechceu.gtceu.client;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Fired when the corresponding GLFW event is triggered, before Minecraft's handling.
 * Cancelling this event will also cancel further handling by Minecraft, like firing the
 * {@link net.minecraftforge.client.event.ScreenEvent.CharacterTyped} event.
 */
@AllArgsConstructor
@Getter
@Cancelable
public class CharTypedEvent extends Event {

    private final char codepoint;
    private final int modifiers;
}
