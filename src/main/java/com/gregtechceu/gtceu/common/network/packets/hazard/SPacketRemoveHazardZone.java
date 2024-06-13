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

@AllArgsConstructor
public class SPacketRemoveHazardZone implements CustomPacketPayload {

    public static final ResourceLocation ID = GTCEu.id("remove_hazard_zone");
    public static final Type<SPacketRemoveHazardZone> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, SPacketRemoveHazardZone> CODEC = StreamCodec
            .ofMember(SPacketRemoveHazardZone::encode, SPacketRemoveHazardZone::decode);

    public ChunkPos pos;

    public void encode(FriendlyByteBuf buf) {
        buf.writeChunkPos(pos);
    }

    public static SPacketRemoveHazardZone decode(FriendlyByteBuf buf) {
        return new SPacketRemoveHazardZone(buf.readChunkPos());
    }

    public static void execute(SPacketRemoveHazardZone packet, IPayloadContext handler) {
        EnvironmentalHazardClientHandler.INSTANCE.removeHazardZone(packet.pos);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
