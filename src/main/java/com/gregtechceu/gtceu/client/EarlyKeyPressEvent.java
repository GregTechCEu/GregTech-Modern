package com.gregtechceu.gtceu.client;

import net.minecraftforge.client.event.InputEvent.Key;
import net.minecraftforge.client.event.ScreenEvent.KeyPressed;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Fired when the corresponding GLFW event is triggered, before Minecraft's handling.
 * Cancelling this event will also cancel further handling by Minecraft, like detecting
 * ESC to pause the game, other controls, and firing the {@link Key} and {@link KeyPressed}
 * events.
 */
@AllArgsConstructor
@Getter
@Cancelable
public class EarlyKeyPressEvent extends Event {

    private final int key;
    private final int scanCode;
    private final int action;
    private final int modifiers;
}
