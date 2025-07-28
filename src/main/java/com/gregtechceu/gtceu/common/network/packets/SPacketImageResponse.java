package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.client.util.ClientImageCache;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;

public class SPacketImageResponse implements GTNetwork.INetPacket {

    private static final int MAX_BYTES_PER_PACKET = 120000;

    private final byte[] imagePart;
    private final String url;
    private final int index;
    private final int totalSize;

    public SPacketImageResponse(String url, byte[] imagePart, int index, int totalSize) {
        this.url = url;
        this.imagePart = imagePart;
        this.index = index;
        this.totalSize = totalSize;
    }

    public SPacketImageResponse(FriendlyByteBuf buf) {
        this.index = buf.readInt();
        this.totalSize = buf.readInt();
        this.url = buf.readUtf();
        this.imagePart = buf.readByteArray();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(index);
        buffer.writeInt(totalSize);
        buffer.writeUtf(url);
        buffer.writeByteArray(imagePart);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        if (imagePart == null) {
            return;
        }
        try {
            ClientImageCache.receiveImagePart(url, imagePart, index, totalSize);
        } catch (IOException ignored) {}
    }

    public static void sendImage(String url, byte[] imageBytes, NetworkEvent.Context context) throws IOException {
        if (imageBytes.length < MAX_BYTES_PER_PACKET) {
            GTNetwork.reply(context, new SPacketImageResponse(url, imageBytes, 0, 1));
        } else {
            int packetCount = GTMath.ceilDiv(imageBytes.length, MAX_BYTES_PER_PACKET);
            int arrayIndex = 0;

            for (int i = 0; i < packetCount; i++) {
                int remaining = imageBytes.length - arrayIndex;
                if (remaining <= 0) {
                    break;
                }

                byte[] part = ArrayUtils.subarray(imageBytes, arrayIndex, arrayIndex + MAX_BYTES_PER_PACKET);
                GTNetwork.reply(context, new SPacketImageResponse(url, part, i, packetCount));

                arrayIndex += MAX_BYTES_PER_PACKET;
            }
        }
    }
}
