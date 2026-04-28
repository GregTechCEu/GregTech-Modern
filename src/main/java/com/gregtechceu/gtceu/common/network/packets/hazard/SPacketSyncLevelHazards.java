package com.gregtechceu.gtceu.common.network.packets.hazard;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.EnvironmentalHazardClientHandler;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class SPacketSyncLevelHazards implements CustomPacketPayload {

    public static final Identifier ID = GTCEu.id("sync_level_hazards");
    public static final Type<SPacketSyncLevelHazards> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, SPacketSyncLevelHazards> CODEC = StreamCodec
            .ofMember(SPacketSyncLevelHazards::encode, SPacketSyncLevelHazards::new);

    private Map<ChunkPos, EnvironmentalHazardSavedData.HazardZone> map;

    public SPacketSyncLevelHazards(FriendlyByteBuf buf) {
        map = Stream.generate(() -> {
            ChunkPos pos = buf.readChunkPos();
            var zone = EnvironmentalHazardSavedData.HazardZone.fromNetwork(buf);
            return Map.entry(pos, zone);
        }).limit(buf.readVarInt()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(map.size());
        for (var entry : map.entrySet()) {
            buf.writeChunkPos(entry.getKey());
            entry.getValue().toNetwork(buf);
        }
    }

    public void execute(IPayloadContext context) {
        if (context.flow() == PacketFlow.CLIENTBOUND) {
            EnvironmentalHazardClientHandler.INSTANCE.updateHazardMap(this.map);
        }
    }

    @Override
    public @NotNull Type<SPacketSyncLevelHazards> type() {
        return TYPE;
    }
}
