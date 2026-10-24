package com.gregtechceu.gtceu.api.placeholder.exceptions;

import net.minecraft.network.chat.Component;

public class WrongNumberOfArgsException extends PlaceholderException {

    public WrongNumberOfArgsException(int expected, int got) {
        super(Component.translatable("gui.gtceu.cover.computer_monitor.error.wrong_number_of_args", expected, got)
                .getString());
    }
}
