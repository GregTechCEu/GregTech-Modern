package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.client.ClientProxy;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.networking.IHandlerContext;
import com.lowdragmc.lowdraglib.networking.IPacket;

import net.minecraft.network.FriendlyByteBuf;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SPacketSyncTickCount implements IPacket {

    private long serverTickCount;

    public SPacketSyncTickCount() {
        serverTickCount = Platform.getMinecraftServer().getTickCount();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarLong(serverTickCount);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        serverTickCount = buf.readVarLong();
    }

    @Override
    public void execute(IHandlerContext handler) {
        ClientProxy.setServerTickCount(serverTickCount);
    }
}
