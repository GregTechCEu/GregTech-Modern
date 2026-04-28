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
public class SPacketSyncHazardZoneStrength implements CustomPacketPayload {

    public static final Identifier ID = GTCEu.id("sync_hazard_zone_strength");
    public static final Type<SPacketSyncHazardZoneStrength> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, SPacketSyncHazardZoneStrength> CODEC = StreamCodec
            .ofMember(SPacketSyncHazardZoneStrength::encode, SPacketSyncHazardZoneStrength::new);

    public ChunkPos pos;
    public float newAmount;

    public SPacketSyncHazardZoneStrength(FriendlyByteBuf buf) {
        pos = buf.readChunkPos();
        this.newAmount = buf.readFloat();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeChunkPos(pos);
        buf.writeFloat(newAmount);
    }

    public void execute(IPayloadContext context) {
        if (context.flow() == PacketFlow.CLIENTBOUND) {
            EnvironmentalHazardClientHandler.INSTANCE.updateHazardStrength(this.pos, this.newAmount);
        }
    }

    @Override
    public @NotNull Type<SPacketSyncHazardZoneStrength> type() {
        return TYPE;
    }
}
