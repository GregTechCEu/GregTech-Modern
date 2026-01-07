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
public class CloseGuiPacket implements GTNetwork.INetPacket {

    private int networkId;
    private boolean dispose;

    public CloseGuiPacket(FriendlyByteBuf buffer) {
        this.networkId = buffer.readVarInt();
        this.dispose = buffer.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.networkId);
        buffer.writeBoolean(this.dispose);
    }

    @Override
    public void execute(NetworkEvent.Context handler) {
        if (handler.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ModularNetwork.CLIENT.closeContainer(this.networkId, this.dispose, Minecraft.getInstance().player, false);
        } else {
            ModularNetwork.SERVER.closeContainer(this.networkId, this.dispose, handler.getSender(), false);
        }
    }
}
