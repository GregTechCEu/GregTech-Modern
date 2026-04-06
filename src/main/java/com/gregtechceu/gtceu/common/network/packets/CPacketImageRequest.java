package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.misc.ImageCache;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class CPacketImageRequest implements CustomPacketPayload {

    public static final ResourceLocation ID = GTCEu.id("image_request");
    public static final Type<CPacketImageRequest> TYPE = new Type<>(ID);
    public static final StreamCodec<ByteBuf, CPacketImageRequest> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CPacketImageRequest::getUrl,
            CPacketImageRequest::new);

    @Getter(AccessLevel.PRIVATE)
    private final String url;

    public CPacketImageRequest(String url) {
        this.url = url;
    }

    public void execute(IPayloadContext context) {
        ImageCache.queryServerImage(url, image -> {
            try {
                SPacketImageResponse.sendImage(url, image, context);
            } catch (IOException ignored) {}
        });
    }

    @Override
    public @NotNull Type<CPacketImageRequest> type() {
        return TYPE;
    }
}
