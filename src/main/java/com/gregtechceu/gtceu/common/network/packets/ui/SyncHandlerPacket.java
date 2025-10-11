package com.gregtechceu.gtceu.common.network.packets.ui;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.value.sync.ModularSyncManager;
import com.gregtechceu.gtceu.client.mui.screen.ModularContainerMenu;
import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.utils.NetworkUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class SyncHandlerPacket implements GTNetwork.INetPacket {

    private String panel;
    private String key;
    private boolean action;
    private FriendlyByteBuf packet;

    @Override
    public void encode(FriendlyByteBuf buf) {
        NetworkUtils.writeStringSafe(buf, this.panel);
        NetworkUtils.writeStringSafe(buf, this.key, 64, true);
        buf.writeBoolean(this.action);
        NetworkUtils.writeByteBuf(buf, this.packet);
    }

    public SyncHandlerPacket(FriendlyByteBuf buf) {
        this.panel = NetworkUtils.readStringSafe(buf);
        this.key = NetworkUtils.readStringSafe(buf);
        this.action = buf.readBoolean();
        this.packet = NetworkUtils.readFriendlyByteBuf(buf);
    }

    @Override
    public void execute(NetworkEvent.Context handler) {
        if (handler.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ModularScreen screen = ModularScreen.getCurrent();
            if (screen != null) {
                executeFromManager(screen.getSyncManager());
            }
        } else {
            AbstractContainerMenu menu = handler.getSender().containerMenu;
            if (menu instanceof ModularContainerMenu modularMenu) {
                executeFromManager(modularMenu.getSyncManager());
            }
        }
    }

    private void executeFromManager(ModularSyncManager syncManager) {
        try {
            int id = this.action ? 0 : this.packet.readVarInt();
            syncManager.receiveWidgetUpdate(this.panel, this.key, this.action, id, this.packet);
        } catch (IndexOutOfBoundsException e) {
            GTCEu.LOGGER.error("Failed to read packet for sync handler {} in panel {}", this.key, this.panel);
        }
    }
}
