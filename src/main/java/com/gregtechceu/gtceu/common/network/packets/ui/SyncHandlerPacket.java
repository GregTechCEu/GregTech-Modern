package com.gregtechceu.gtceu.common.network.packets.ui;

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
    private FriendlyByteBuf packet;

    @Override
    public void encode(FriendlyByteBuf buf) {
        NetworkUtils.writeStringSafe(buf, this.panel);
        NetworkUtils.writeStringSafe(buf, this.key, 64, true);
        NetworkUtils.writeByteBuf(buf, this.packet);
    }

    public SyncHandlerPacket(FriendlyByteBuf buf) {
        this.panel = NetworkUtils.readStringSafe(buf);
        this.key = NetworkUtils.readStringSafe(buf);
        this.packet = NetworkUtils.readFriendlyByteBuf(buf);
    }

    @Override
    public void execute(NetworkEvent.Context handler) {
        if (handler.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ModularScreen screen = ModularScreen.getCurrent();
            if (screen != null) {
                screen.getSyncManager().receiveWidgetUpdate(this.panel, this.key, this.packet.readVarInt(),
                        this.packet);
            }
        } else {
            AbstractContainerMenu menu = handler.getSender().containerMenu;
            if (menu instanceof ModularContainerMenu modularMenu) {
                modularMenu.getSyncManager()
                        .receiveWidgetUpdate(this.panel, this.key, this.packet.readVarInt(), this.packet);
            }
        }
    }
}
