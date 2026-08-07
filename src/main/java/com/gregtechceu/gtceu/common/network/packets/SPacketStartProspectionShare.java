package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.map.ClientCacheManager;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;

/**
 * Sent from the server to the sender's client to kick off sharing their local prospection cache.
 * The {@code /gtceu share_prospection_data} command is a server command. The prospection cache
 * only exists on the client, so the server delegates the actual read + send back to the sender's
 * client via this packet.
 */
@AllArgsConstructor
public class SPacketStartProspectionShare implements CustomPacketPayload {

    public static final ResourceLocation ID = GTCEu.id("share_prospection_start");
    public static final CustomPacketPayload.Type<SPacketStartProspectionShare> TYPE = new CustomPacketPayload.Type<>(
            ID);
    public static final StreamCodec<FriendlyByteBuf, SPacketStartProspectionShare> CODEC = StreamCodec
            .ofMember(SPacketStartProspectionShare::encode, SPacketStartProspectionShare::new);

    private UUID receiver;

    public SPacketStartProspectionShare(FriendlyByteBuf buf) {
        receiver = buf.readUUID();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(receiver);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void execute(IPayloadContext context) {
        UUID sender = Minecraft.getInstance().player.getUUID();
        Thread sendThread = new Thread(new ProspectingShareTask(sender, receiver));
        sendThread.start();
    }

    private static class ProspectingShareTask implements Runnable {

        private final List<ClientCacheManager.ProspectionInfo> prospectionData;
        private final UUID sender;
        private final UUID receiver;

        public ProspectingShareTask(UUID sender, UUID receiver) {
            prospectionData = ClientCacheManager.getProspectionShareData();
            this.sender = sender;
            this.receiver = receiver;
        }

        @Override
        public void run() {
            boolean first = true;
            for (ClientCacheManager.ProspectionInfo info : prospectionData) {
                PacketDistributor.sendToServer(new SCPacketShareProspection(sender, receiver, info.cacheName, info.key,
                        info.isDimCache, info.dim, info.data, first));
                first = false;

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
