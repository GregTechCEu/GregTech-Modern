package com.gregtechceu.gtceu.api.placeholder.exceptions;

import net.minecraft.network.chat.Component;

public class InvalidArgsException extends PlaceholderException {

    public InvalidArgsException() {
        super(Component.translatable("gui.gtceu.cover.computer_monitor.error.invalid_args").getString());
    }
}
