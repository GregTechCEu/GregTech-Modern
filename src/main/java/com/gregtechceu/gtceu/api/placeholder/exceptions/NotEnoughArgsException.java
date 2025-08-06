package com.gregtechceu.gtceu.api.placeholder.exceptions;

import net.minecraft.network.chat.Component;

public class NotEnoughArgsException extends PlaceholderException {

    public NotEnoughArgsException(int expected, int got) {
        super(Component.translatable("gtceu.computer_monitor_cover.error.not_enough_args", expected, got).getString());
    }
}
