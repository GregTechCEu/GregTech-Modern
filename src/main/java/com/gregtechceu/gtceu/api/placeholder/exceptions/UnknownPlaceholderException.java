package com.gregtechceu.gtceu.api.placeholder.exceptions;

import net.minecraft.network.chat.Component;

public class UnknownPlaceholderException extends PlaceholderException {

    public UnknownPlaceholderException(String name) {
        super(Component.translatable("gui.gtceu.cover.computer_monitor.error.no_placeholder", name).getString());
    }
}
