package com.gregtechceu.gtceu.common.network.packets.ui;

import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.ModularNetwork;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class ReopenGuiPacket implements GTNetwork.INetPacket {

    private int networkId;

    public ReopenGuiPacket(FriendlyByteBuf buffer) {
        this.networkId = buffer.readVarInt();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(networkId);
    }

    @Override
    public void execute(NetworkEvent.Context handler) {
        if (handler.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ModularNetwork.CLIENT.reopen(Minecraft.getInstance().player, this.networkId, false);
        } else {
            ModularNetwork.SERVER.reopen(handler.getSender(), this.networkId, false);
        }
    }
}
