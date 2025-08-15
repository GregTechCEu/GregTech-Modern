package com.gregtechceu.gtceu.api.placeholder.exceptions;

import net.minecraft.network.chat.Component;

public class UnexpectedBracketException extends RuntimeException {

    public UnexpectedBracketException() {
        super(Component.translatable("gtceu.computer_monitor_cover.error.unexpected_bracket").getString());
    }
}
