package com.gregtechceu.gtceu.common.placeholders.exceptions;

import net.minecraft.network.chat.Component;

public class InvalidArgsException extends PlaceholderException {

    public InvalidArgsException() {
        super(Component.translatable("gtceu.computer_monitor_cover.error.invalid_args").getString());
    }
}
