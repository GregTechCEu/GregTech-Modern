package com.gregtechceu.gtceu.api.placeholder.exceptions;

import net.minecraft.network.chat.Component;

public class InvalidNumberException extends PlaceholderException {

    public InvalidNumberException(String number) {
        super(Component.translatable("gtceu.computer_monitor_cover.error.invalid_number", number).getString());
    }
}
