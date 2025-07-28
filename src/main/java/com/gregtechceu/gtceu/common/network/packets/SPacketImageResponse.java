package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import com.mojang.blaze3d.platform.NativeImage;

import java.io.IOException;

public class SPacketImageResponse implements GTNetwork.INetPacket {

    private static final int BYTES_PER_PACKET = 120000;

    private final byte[] image;
    private final String url;
    private final int index;
    private final int totalSize;

    public SPacketImageResponse(String url, byte[] imagePart, int index, int totalSize) {
        this.url = url;
        this.image = imagePart;
        this.index = index;
        this.totalSize = totalSize;
    }

    public SPacketImageResponse(FriendlyByteBuf buf) {
        GTCEu.LOGGER.info("Decoding packet!");
        this.index = buf.readInt();
        GTCEu.LOGGER.info("Packet index = {}", index);
        this.totalSize = buf.readInt();
        GTCEu.LOGGER.info("Packet totalSize = {}", totalSize);
        this.url = buf.readUtf();
        GTCEu.LOGGER.info("Packet url = {}", url);
        this.image = buf.readByteArray(BYTES_PER_PACKET);
        GTCEu.LOGGER.info("Decoded packet, part size = {}", image.length);
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(index);
        buffer.writeInt(totalSize);
        buffer.writeUtf(url);
        buffer.writeByteArray(image);
        GTCEu.LOGGER.info("Encoded image packet, index = {}, totalSize = {}, part size = {}, url = {}", index,
                totalSize, image.length, url);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        if (image == null) {
            GTCEu.LOGGER.info("Received image packet, index = {}, totalSize = {}, ignoring as image is null", index,
                    totalSize);
            return;
        }
        GTCEu.LOGGER.info("Received image packet, index = {}, totalSize = {}, part size = {}, for url = {}", index,
                totalSize, image.length, url);
        try {
            GTCEu.IMAGE_CACHE.receiveImagePart(url, image, index, totalSize);
        } catch (IOException ignored) {}
    }

    public static void sendImage(String url, NativeImage image, ServerPlayer player) throws IOException {
        GTNetwork.sendToPlayer(player, new SPacketImageResponse(url, image.asByteArray(), 0, 1));
    }
}
