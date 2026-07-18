package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.integration.map.ClientCacheManager;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Sent from the server to the sender's client to kick off sharing their local prospection cache.
 *
 * The {@code /gtceu share_prospection_data} command is a server command. The prospection cache
 * only exists on the client, so the server delegates the actual read + send back to the sender's
 * client via this packet.
 */
@AllArgsConstructor
public class SPacketStartProspectionShare implements GTNetwork.INetPacket {

    private UUID receiver;

    @SuppressWarnings("unused")
    public SPacketStartProspectionShare() {}

    public SPacketStartProspectionShare(FriendlyByteBuf buf) {
        receiver = buf.readUUID();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(receiver);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
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
                GTNetwork.sendToServer(new SCPacketShareProspection(sender, receiver, info.cacheName, info.key,
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
