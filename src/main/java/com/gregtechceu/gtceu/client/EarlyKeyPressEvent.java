package com.gregtechceu.gtceu.client;

import net.neoforged.bus.api.Event;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Fired when the corresponding GLFW event is triggered, before Minecraft's handling.
 * Cancelling this event will also cancel further handling by Minecraft, like detecting
 * ESC to pause the game, other controls, and firing the {@link Key} and {@link KeyPressed}
 * events.
 */
@Getter
public class EarlyKeyPressEvent extends Event {

    @Getter
    @Setter
    @Accessors(fluent = true)
    private boolean cancelled = false;

    private final int key;
    private final int scanCode;
    private final int action;
    private final int modifiers;

    public EarlyKeyPressEvent(int key, int scanCode, int action, int modifiers) {
        this.key = key;
        this.scanCode = scanCode;
        this.action = action;
        this.modifiers = modifiers;
    }
}
