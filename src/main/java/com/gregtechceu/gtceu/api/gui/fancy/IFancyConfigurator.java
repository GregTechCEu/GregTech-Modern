package com.gregtechceu.gtceu.api.gui.fancy;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface IFancyConfigurator {

    Component getTitle();

    IGuiTexture getIcon();

    Widget createConfigurator();

    default List<Component> getTooltips() {
        return List.of(getTitle());
    }

    default void detectAndSendChange(BiConsumer<Integer, Consumer<RegistryFriendlyByteBuf>> sender) {}

    default void readUpdateInfo(int id, RegistryFriendlyByteBuf buf) {}

    default void writeInitialData(RegistryFriendlyByteBuf buffer) {}

    default void readInitialData(RegistryFriendlyByteBuf buffer) {}
}
