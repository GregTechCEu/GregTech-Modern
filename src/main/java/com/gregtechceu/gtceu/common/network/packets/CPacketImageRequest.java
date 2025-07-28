package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.api.misc.ImageCache;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.io.IOException;

public class CPacketImageRequest implements GTNetwork.INetPacket {

    private final String url;

    public CPacketImageRequest(String url) {
        this.url = url;
    }

    public CPacketImageRequest(FriendlyByteBuf buf) {
        this.url = buf.readUtf();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(url);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        ImageCache.queryServerImage(url, image -> {
            try {
                SPacketImageResponse.sendImage(url, image, context);
            } catch (IOException ignored) {}
        });
    }
}
