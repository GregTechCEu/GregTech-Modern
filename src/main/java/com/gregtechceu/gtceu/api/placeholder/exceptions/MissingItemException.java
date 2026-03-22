package com.gregtechceu.gtceu.api.placeholder.exceptions;

import net.minecraft.network.chat.Component;

public class MissingItemException extends PlaceholderException {

    public MissingItemException(String item, int slot) {
        super(Component.translatable("gtceu.computer_monitor_cover.error.missing_item", item, slot).getString());
    }
}
