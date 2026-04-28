package com.gregtechceu.gtceu.common.network.packets.hazard;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.EnvironmentalHazardClientHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class SPacketRemoveHazardZone implements CustomPacketPayload {

    public static final Identifier ID = GTCEu.id("remove_hazard_zone");
    public static final Type<SPacketRemoveHazardZone> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, SPacketRemoveHazardZone> CODEC = StreamCodec
            .ofMember(SPacketRemoveHazardZone::encode, SPacketRemoveHazardZone::new);

    public ChunkPos pos;

    public SPacketRemoveHazardZone(FriendlyByteBuf buf) {
        pos = buf.readChunkPos();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeChunkPos(pos);
    }

    public void execute(IPayloadContext context) {
        if (context.flow() == PacketFlow.CLIENTBOUND) {
            EnvironmentalHazardClientHandler.INSTANCE.removeHazardZone(pos);
        }
    }

    @Override
    public @NotNull Type<SPacketRemoveHazardZone> type() {
        return TYPE;
    }
}
