package com.gregtechceu.gtceu.api.placeholder.exceptions;

import net.minecraft.network.chat.Component;

public class NoMENetworkException extends PlaceholderException {

    public NoMENetworkException() {
        super(Component.translatable("gui.gtceu.cover.computer_monitor.error.no_ae").getString());
    }
}
