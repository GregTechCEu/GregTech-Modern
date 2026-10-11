package com.gregtechceu.gtceu.api.placeholder.exceptions;

import net.minecraft.network.chat.Component;

public class NotSupportedException extends PlaceholderException {

    public NotSupportedException() {
        super(Component.translatable("gui.gtceu.cover.computer_monitor.error.not_supported").getString());
    }
}
