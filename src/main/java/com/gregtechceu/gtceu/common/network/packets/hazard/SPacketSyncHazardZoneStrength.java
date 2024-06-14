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
public class SPacketSyncHazardZoneStrength implements IPacket {

    public ChunkPos pos;
    public int newAmount;

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarLong(pos.toLong());
        buf.writeVarInt(newAmount);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        long packed = buf.readVarLong();
        int x = ChunkPos.getX(packed);
        int z = ChunkPos.getZ(packed);
        pos = new ChunkPos(x, z);
        this.newAmount = buf.readVarInt();
    }

    @Override
    public void execute(IHandlerContext handler) {
        if (handler.isClient()) {
            EnvironmentalHazardClientHandler.INSTANCE.updateHazardStrength(pos, newAmount);
        }
    }
}
