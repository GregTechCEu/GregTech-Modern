package com.gregtechceu.gtceu.common.network.packets.hazard;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.EnvironmentalHazardClientHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class SPacketSyncHazardZoneStrength implements CustomPacketPayload {

    public static final ResourceLocation ID = GTCEu.id("sync_hazard_zone_strength");
    public static final Type<SPacketSyncHazardZoneStrength> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, SPacketSyncHazardZoneStrength> CODEC = StreamCodec
            .ofMember(SPacketSyncHazardZoneStrength::encode, SPacketSyncHazardZoneStrength::decode);

    public ChunkPos pos;
    public float newAmount;

    public void encode(FriendlyByteBuf buf) {
        buf.writeChunkPos(pos);
        buf.writeFloat(newAmount);
    }

    public static SPacketSyncHazardZoneStrength decode(FriendlyByteBuf buf) {
        ChunkPos pos = buf.readChunkPos();
        float newAmount = buf.readFloat();
        return new SPacketSyncHazardZoneStrength(pos, newAmount);
    }

    public static void execute(SPacketSyncHazardZoneStrength packet, IPayloadContext handler) {
        EnvironmentalHazardClientHandler.INSTANCE.updateHazardStrength(packet.pos, packet.newAmount);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
