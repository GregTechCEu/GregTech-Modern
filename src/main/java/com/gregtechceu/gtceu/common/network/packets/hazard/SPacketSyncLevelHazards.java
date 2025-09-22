package com.gregtechceu.gtceu.common.network.packets.hazard;

import com.gregtechceu.gtceu.client.EnvironmentalHazardClientHandler;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor
@AllArgsConstructor
public class SPacketSyncLevelHazards implements GTNetwork.INetPacket {

    private Map<ChunkPos, EnvironmentalHazardSavedData.HazardZone> map;

    public SPacketSyncLevelHazards(FriendlyByteBuf buf) {
        map = Stream.generate(() -> {
            ChunkPos pos = buf.readChunkPos();
            var zone = EnvironmentalHazardSavedData.HazardZone.fromNetwork(buf);
            return Map.entry(pos, zone);
        }).limit(buf.readVarInt()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(map.size());
        for (var entry : map.entrySet()) {
            buf.writeChunkPos(entry.getKey());
            entry.getValue().toNetwork(buf);
        }
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            EnvironmentalHazardClientHandler.INSTANCE.updateHazardMap(this.map);
        }
    }
}
