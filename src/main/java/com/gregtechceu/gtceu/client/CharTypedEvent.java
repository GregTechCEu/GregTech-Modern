package com.gregtechceu.gtceu.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@AllArgsConstructor
@Getter
@Cancelable
public class CharTypedEvent extends Event {
    private final char codepoint;
    private final int modifiers;
}
