package com.gregtechceu.gtceu.common.placeholders.exceptions;

import net.minecraft.network.chat.Component;

public class NotSupportedException extends PlaceholderException {

    public NotSupportedException() {
        super(Component.translatable("gtceu.computer_monitor_cover.error.not_supported").getString());
    }
}
