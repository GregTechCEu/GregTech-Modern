package com.gregtechceu.gtceu.api.placeholder.exceptions;

import net.minecraft.network.chat.Component;

public class NoMENetworkException extends PlaceholderException {

    public NoMENetworkException() {
        super(Component.translatable("gtceu.computer_monitor_cover.error.no_ae").getString());
    }
}
