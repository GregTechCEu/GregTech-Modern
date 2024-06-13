package com.gregtechceu.gtceu.common.network.packets.hazard;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.EnvironmentalHazardClientHandler;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor
@AllArgsConstructor
public class SPacketSyncLevelHazards implements CustomPacketPayload {

    public static final ResourceLocation ID = GTCEu.id("sync_level_hazards");
    public static final Type<SPacketSyncLevelHazards> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, SPacketSyncLevelHazards> CODEC = StreamCodec
            .ofMember(SPacketSyncLevelHazards::encode, SPacketSyncLevelHazards::decode);

    private Map<ChunkPos, EnvironmentalHazardSavedData.HazardZone> map;

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(map.size());
        for (var entry : map.entrySet()) {
            buf.writeChunkPos(entry.getKey());
            entry.getValue().toNetwork(buf);
        }
    }

    public static SPacketSyncLevelHazards decode(FriendlyByteBuf buf) {
        var map = Stream.generate(() -> {
            ChunkPos pos = buf.readChunkPos();
            var zone = EnvironmentalHazardSavedData.HazardZone.fromNetwork(buf);
            return Map.entry(pos, zone);
        }).limit(buf.readVarInt()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new SPacketSyncLevelHazards(map);
    }

    public static void execute(SPacketSyncLevelHazards packet, IPayloadContext handler) {
        EnvironmentalHazardClientHandler.INSTANCE.updateHazardMap(packet.map);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
