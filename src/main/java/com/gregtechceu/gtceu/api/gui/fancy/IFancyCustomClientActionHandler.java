package com.gregtechceu.gtceu.api.gui.fancy;

import net.minecraft.network.FriendlyByteBuf;

public interface IFancyCustomClientActionHandler {

    default void handleClientAction(int id, FriendlyByteBuf buffer) {}
}
