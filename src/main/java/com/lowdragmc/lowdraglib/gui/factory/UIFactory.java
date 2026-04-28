package com.lowdragmc.lowdraglib.gui.factory;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public abstract class UIFactory<T> {

    public static final Map<ResourceLocation, UIFactory<?>> FACTORIES = new HashMap<>();
    public final ResourceLocation uiFactoryId;

    public UIFactory(ResourceLocation uiFactoryId) {
        this.uiFactoryId = uiFactoryId;
    }

    public static void register(UIFactory<?> factory) {
        FACTORIES.put(factory.uiFactoryId, factory);
    }

    public final boolean openUI(T holder, ServerPlayer player) {
        return createUITemplate(holder, player) != null;
    }

    public final void initClientUI(RegistryFriendlyByteBuf syncData, int windowId) {
        T holder = readHolderFromSyncData(syncData);
        if (holder != null) {
            createUITemplate(holder, net.minecraft.client.Minecraft.getInstance().player);
        }
    }

    protected abstract ModularUI createUITemplate(T holder, Player entityPlayer);

    protected abstract T readHolderFromSyncData(RegistryFriendlyByteBuf syncData);

    protected abstract void writeHolderToSyncData(RegistryFriendlyByteBuf syncData, T holder);
}
