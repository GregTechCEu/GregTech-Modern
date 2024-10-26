package com.gregtechceu.gtceu.common.network.packets.hazard;

import com.gregtechceu.gtceu.client.EnvironmentalHazardClientHandler;

import com.lowdragmc.lowdraglib.networking.IHandlerContext;
import com.lowdragmc.lowdraglib.networking.IPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class SPacketRemoveHazardZone implements IPacket {

    public ChunkPos pos;

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeChunkPos(pos);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        pos = buf.readChunkPos();
    }

    @Override
    public void execute(IHandlerContext handler) {
        if (handler.isClient()) {
            EnvironmentalHazardClientHandler.INSTANCE.removeHazardZone(pos);
        }
    }
}
