package com.gregtechceu.gtceu.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@AllArgsConstructor
@Getter
@Cancelable
public class EarlyKeyPressEvent extends Event {
    private final int key;
    private final int scanCode;
    private final int action;
    private final int modifiers;
}
