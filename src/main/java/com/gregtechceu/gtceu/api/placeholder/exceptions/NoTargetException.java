package com.gregtechceu.gtceu.api.placeholder.exceptions;

import net.minecraft.network.chat.Component;

public class NoTargetException extends PlaceholderException {

    public NoTargetException() {
        super(Component.translatable("gtceu.computer_monitor_cover.error.no_target").getString());
    }
}
