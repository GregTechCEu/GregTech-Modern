package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.client.renderer.AABBHighlightRenderer;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Highlights the positions the autobuilder could not place blocks at.
 */
@NoArgsConstructor
@AllArgsConstructor
public class SPacketAutobuildHighlight implements GTNetwork.INetPacket {

    public long[] positions;

    public SPacketAutobuildHighlight(FriendlyByteBuf buf) {
        positions = buf.readLongArray();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeLongArray(positions);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
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
}
