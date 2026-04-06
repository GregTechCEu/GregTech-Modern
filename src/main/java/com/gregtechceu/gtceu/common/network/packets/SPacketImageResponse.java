package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.util.ClientImageCache;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class SPacketImageResponse implements CustomPacketPayload {

    public static final ResourceLocation ID = GTCEu.id("image_response");
    public static final CustomPacketPayload.Type<SPacketImageResponse> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, SPacketImageResponse> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SPacketImageResponse::getUrl,
            ByteBufCodecs.BYTE_ARRAY, SPacketImageResponse::getImagePart,
            ByteBufCodecs.VAR_INT, SPacketImageResponse::getIndex,
            ByteBufCodecs.VAR_INT, SPacketImageResponse::getTotalSize,
            SPacketImageResponse::new);

    private static final int MAX_BYTES_PER_PACKET = 120000;

    @Getter(AccessLevel.PRIVATE)
    private final String url;
    @Getter(AccessLevel.PRIVATE)
    private final byte[] imagePart;
    @Getter(AccessLevel.PRIVATE)
    private final int index;
    @Getter(AccessLevel.PRIVATE)
    private final int totalSize;

    public SPacketImageResponse(String url, byte[] imagePart, int index, int totalSize) {
        this.url = url;
        this.imagePart = imagePart;
        this.index = index;
        this.totalSize = totalSize;
    }

    public void execute(IPayloadContext context) {
        if (this.imagePart == null) {
            return;
        }
        try {
            ClientImageCache.receiveImagePart(this.url, this.imagePart, this.index, this.totalSize);
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
