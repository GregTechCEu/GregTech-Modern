package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.util.ClientImageCache;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class SPacketImageResponse implements CustomPacketPayload {

    public static final ResourceLocation ID = GTCEu.id("packet_image_response");
    public static final Type<SPacketImageResponse> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, SPacketImageResponse> CODEC = StreamCodec
            .ofMember(SPacketImageResponse::encode, SPacketImageResponse::new);

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

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(index);
        buffer.writeInt(totalSize);
        buffer.writeUtf(url);
        buffer.writeByteArray(imagePart);
    }

    public void execute(IPayloadContext context) {
        if (imagePart == null) {
            return;
        }
        try {
            ClientImageCache.receiveImagePart(url, imagePart, index, totalSize);
        } catch (IOException ignored) {}
    }

    public static void sendImage(String url, byte[] imageBytes, IPayloadContext context) throws IOException {
        if (imageBytes.length < MAX_BYTES_PER_PACKET) {
            context.reply(new SPacketImageResponse(url, imageBytes, 0, 1));
        } else {
            int packetCount = GTMath.ceilDiv(imageBytes.length, MAX_BYTES_PER_PACKET);
            int arrayIndex = 0;

            for (int i = 0; i < packetCount; i++) {
                int remaining = imageBytes.length - arrayIndex;
                if (remaining <= 0) {
                    break;
                }

                byte[] part = ArrayUtils.subarray(imageBytes, arrayIndex, arrayIndex + MAX_BYTES_PER_PACKET);
                context.reply(new SPacketImageResponse(url, part, i, packetCount));

                arrayIndex += MAX_BYTES_PER_PACKET;
            }
        }
    }

    @Override
    public @NotNull Type<SPacketImageResponse> type() {
        return TYPE;
    }
}
