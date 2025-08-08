package com.gregtechceu.gtceu.api.placeholder.exceptions;

import net.minecraft.network.chat.Component;

public class UnclosedBracketException extends PlaceholderException {

    public UnclosedBracketException() {
        super(Component.translatable("gtceu.computer_monitor_cover.error.unclosed_bracket").getString());
    }
}
