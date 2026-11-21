package com.gregtechceu.gtceu.api.placeholder.exceptions;

import net.minecraft.network.chat.Component;

public class InvalidNumberException extends PlaceholderException {

    public InvalidNumberException(String number) {
        super(Component.translatable("gui.gtceu.cover.computer_monitor.error.invalid_number", number).getString());
    }
}
