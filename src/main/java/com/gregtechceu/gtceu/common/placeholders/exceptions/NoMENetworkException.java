package com.gregtechceu.gtceu.common.placeholders.exceptions;

import net.minecraft.network.chat.Component;

public class NoMENetworkException extends PlaceholderException {

    public NoMENetworkException() {
        super(Component.translatable("gtceu.computer_monitor_cover.error.no_ae").getString());
    }
}
