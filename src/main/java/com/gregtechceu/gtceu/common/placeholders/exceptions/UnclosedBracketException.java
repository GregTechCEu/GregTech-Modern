package com.gregtechceu.gtceu.common.placeholders.exceptions;

import net.minecraft.network.chat.Component;

public class UnclosedBracketException extends PlaceholderException {

    public UnclosedBracketException() {
        super(Component.translatable("gtceu.computer_monitor_cover.error.unclosed_bracket").getString());
    }
}
