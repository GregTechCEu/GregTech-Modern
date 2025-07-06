package com.gregtechceu.gtceu.common.network.packets.hazard;

import com.gregtechceu.gtceu.client.EnvironmentalHazardClientHandler;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class SPacketSyncHazardZoneStrength implements GTNetwork.INetPacket {

    public ChunkPos pos;
    public float newAmount;

    public SPacketSyncHazardZoneStrength(FriendlyByteBuf buf) {
        pos = buf.readChunkPos();
        this.newAmount = buf.readFloat();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeChunkPos(pos);
        buf.writeFloat(newAmount);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            EnvironmentalHazardClientHandler.INSTANCE.updateHazardStrength(pos, newAmount);
        }
    }
}
