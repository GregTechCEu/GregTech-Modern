package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.SyncSystemCodecs;
import com.gregtechceu.gtceu.client.renderer.AABBHighlightRenderer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Highlights the positions the autobuilder could not place blocks at.
 */
@NoArgsConstructor
@AllArgsConstructor
public class SPacketAutobuildHighlight implements CustomPacketPayload {

    public static final ResourceLocation ID = GTCEu.id("autobuild_highlight");
    public static final CustomPacketPayload.Type<SPacketAutobuildHighlight> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SPacketAutobuildHighlight> CODEC = SyncSystemCodecs.LONG_ARRAY_STREAM
            .map(SPacketAutobuildHighlight::new, v -> v.positions);

    public long[] positions;

    public SPacketAutobuildHighlight(FriendlyByteBuf buf) {
        positions = buf.readLongArray();
    }

    public void execute(IPayloadContext context) {
        if (context.flow() == PacketFlow.CLIENTBOUND) {
            for (long pos : positions) {
                AABBHighlightRenderer.INSTANCE.addHighlight(AABBHighlightRenderer.builder()
                        .aabb(BlockPos.of(pos))
                        .colorARGB(255, 180, 0, 0)
                        .thickness(0.025)
                        .durationMillis(10000)
                        .phaseMillis(750)
                        .build());
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
