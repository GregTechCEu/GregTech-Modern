package com.gregtechceu.gtceu.common.network.packets.ui;

import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.ModularNetwork;
import com.gregtechceu.gtceu.utils.NetworkUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.ApiStatus;

@NoArgsConstructor
@AllArgsConstructor
@ApiStatus.Internal
public class SyncHandlerPacket implements GTNetwork.INetPacket {

    public int networkId;
    public String panel;
    public String key;
    public boolean action;
    public FriendlyByteBuf packet;

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.networkId);
        NetworkUtils.writeStringSafe(buf, this.panel, 256, true);
        NetworkUtils.writeStringSafe(buf, this.key, 256, true);
        buf.writeBoolean(this.action);
        NetworkUtils.writeByteBuf(buf, this.packet);
    }

    public SyncHandlerPacket(FriendlyByteBuf buf) {
        this.networkId = buf.readVarInt();
        this.panel = NetworkUtils.readStringSafe(buf);
        this.key = NetworkUtils.readStringSafe(buf);
        this.action = buf.readBoolean();
        this.packet = NetworkUtils.readFriendlyByteBuf(buf);
    }

    @Override
    public void execute(NetworkEvent.Context handler) {
        if (handler.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ModularNetwork.CLIENT.receivePacket(this);
        } else {
            ModularNetwork.SERVER.receivePacket(this);
        }
    }
}
