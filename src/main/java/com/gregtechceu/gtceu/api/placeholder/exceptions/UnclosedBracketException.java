package com.gregtechceu.gtceu.api.placeholder.exceptions;

import net.minecraft.network.chat.Component;

public class UnclosedBracketException extends PlaceholderException {

    public UnclosedBracketException() {
        super(Component.translatable("gui.gtceu.cover.computer_monitor.error.unclosed_bracket").getString());
    }
}
