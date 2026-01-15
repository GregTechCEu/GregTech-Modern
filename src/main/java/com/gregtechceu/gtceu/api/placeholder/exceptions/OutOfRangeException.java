package com.gregtechceu.gtceu.api.placeholder.exceptions;

import net.minecraft.network.chat.Component;

public class OutOfRangeException extends PlaceholderException {

    public OutOfRangeException(String what, int min, int max, int provided) {
        super(Component.translatable("gtceu.computer_monitor_cover.error.not_in_range", what, min, max, provided)
                .getString());
    }
}
