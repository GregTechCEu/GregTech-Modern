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

@AllArgsConstructor
public class SPacketAddHazardZone implements CustomPacketPayload {

    public static final ResourceLocation ID = GTCEu.id("add_hazard_zone");
    public static final Type<SPacketAddHazardZone> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, SPacketAddHazardZone> CODEC = StreamCodec
            .ofMember(SPacketAddHazardZone::encode, SPacketAddHazardZone::decode);

    private ChunkPos pos;
    private EnvironmentalHazardSavedData.HazardZone zone;

    public void encode(FriendlyByteBuf buf) {
        buf.writeChunkPos(pos);
        zone.toNetwork(buf);
    }

    public static SPacketAddHazardZone decode(FriendlyByteBuf buf) {
        ChunkPos pos = buf.readChunkPos();
        var zone = EnvironmentalHazardSavedData.HazardZone.fromNetwork(buf);
        return new SPacketAddHazardZone(pos, zone);
    }

    public static void execute(SPacketAddHazardZone packet, IPayloadContext handler) {
        EnvironmentalHazardClientHandler.INSTANCE.addHazardZone(packet.pos, packet.zone);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
