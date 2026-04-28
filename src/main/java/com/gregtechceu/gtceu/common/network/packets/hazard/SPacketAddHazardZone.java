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

@AllArgsConstructor
public class SPacketAddHazardZone implements CustomPacketPayload {

    public static final Identifier ID = GTCEu.id("add_hazard_zone");
    public static final Type<SPacketAddHazardZone> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, SPacketAddHazardZone> CODEC = StreamCodec
            .ofMember(SPacketAddHazardZone::encode, SPacketAddHazardZone::new);

    private ChunkPos pos;
    private EnvironmentalHazardSavedData.HazardZone zone;

    public SPacketAddHazardZone(FriendlyByteBuf buf) {
        pos = buf.readChunkPos();
        zone = EnvironmentalHazardSavedData.HazardZone.fromNetwork(buf);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeChunkPos(pos);
        zone.toNetwork(buf);
    }

    public void execute(IPayloadContext context) {
        if (context.flow() == PacketFlow.CLIENTBOUND) {
            EnvironmentalHazardClientHandler.INSTANCE.addHazardZone(this.pos, this.zone);
        }
    }

    @Override
    public @NotNull Type<SPacketAddHazardZone> type() {
        return TYPE;
    }
}
