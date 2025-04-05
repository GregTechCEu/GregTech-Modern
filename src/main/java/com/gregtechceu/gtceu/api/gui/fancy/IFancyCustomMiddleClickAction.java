package com.gregtechceu.gtceu.api.gui.fancy;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface IFancyCustomMiddleClickAction extends IFancyCustomClientActionHandler {

    default void onMiddleClick(BiConsumer<Integer, Consumer<FriendlyByteBuf>> writeClientAction) {}
}
