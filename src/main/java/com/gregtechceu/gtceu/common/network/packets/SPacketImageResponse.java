package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.client.util.ClientImageCache;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.io.IOException;

public class SPacketImageResponse implements GTNetwork.INetPacket {

    private static final int BYTES_PER_PACKET = 120000;

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
        this.imagePart = buf.readByteArray(BYTES_PER_PACKET);
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

    public static void sendImage(String url, byte[] imagePart, NetworkEvent.Context context) throws IOException {
        GTNetwork.reply(context, new SPacketImageResponse(url, imagePart, 0, 1));
    }
}
