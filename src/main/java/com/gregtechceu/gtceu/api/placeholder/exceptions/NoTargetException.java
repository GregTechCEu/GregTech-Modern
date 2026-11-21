package com.gregtechceu.gtceu.api.placeholder.exceptions;

import net.minecraft.network.chat.Component;

public class NoTargetException extends PlaceholderException {

    public NoTargetException() {
        super(Component.translatable("gui.gtceu.cover.computer_monitor.error.no_target").getString());
    }
}
