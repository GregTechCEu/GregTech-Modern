package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.misc.ImageCache;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.CPacketImageRequest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.platform.NativeImage;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@OnlyIn(Dist.CLIENT)
public class ClientImageCache {

    private static final Map<String, byte[][]> imageParts = new HashMap<>();

    private static boolean downloading = false;
    // TODO make some kind of loading icon for this
    private static final AbstractTexture LOADING_TEXTURE_MARKER = new SimpleTexture(
            GTCEu.id("textures/block/void.png"));
    private static final LoadingCache<String, AbstractTexture> CACHE = CacheBuilder.newBuilder()
            .refreshAfterWrite(ImageCache.REFRESH_SECS, TimeUnit.SECONDS)
            .expireAfterAccess(ImageCache.EXPIRE_SECS, TimeUnit.SECONDS)
            .build(CacheLoader.from(url -> {
                if (!downloading) {
                    downloading = true;
                    GTCEu.LOGGER.debug("Requesting image {}", url);
                    GTNetwork.sendToServer(new CPacketImageRequest(url));
                }
                return LOADING_TEXTURE_MARKER;
            }));

    private static @NotNull ResourceLocation getUrlTextureId(String url) {
        return GTCEu.id("textures/central_monitor/image_" + url.hashCode());
    }

    public static @Nullable ResourceLocation getOrLoadTexture(String url) {
        AbstractTexture texture = null;

        try {
            texture = CACHE.get(url);
        } catch (ExecutionException e) {
            Throwable t = e;
            if (t.getCause() != null) {
                t = t.getCause();
            }
            GTCEu.LOGGER.error("Could not load image {}", url, t);
        }
        if (texture == null || texture == LOADING_TEXTURE_MARKER) {
            return null;
        }

        return getUrlTextureId(url);
    }

    @ApiStatus.Internal
    public static void receiveImagePart(String url, byte[] imagePart, int index,
                                        final int totalParts) throws IOException {
        byte[][] parts = imageParts.computeIfAbsent(url, $ -> new byte[totalParts][]);
        parts[index] = imagePart;

        if (index == totalParts - 1) {
            byte[] imageBytes = new byte[imagePart.length];
            int currentIndex = 0;
            for (byte[] part : parts) {
                imageBytes = ArrayUtils.insert(currentIndex, imageBytes, part);
                currentIndex += part.length;
            }

            saveTexture(url, imageBytes);
            imageParts.remove(url);
            downloading = false;
        }
    }

    private static void saveTexture(String url, byte[] imageBytes) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(imageBytes.length);
        buffer.put(imageBytes).flip();
        DynamicTexture texture = new DynamicTexture(NativeImage.read(buffer));

        Minecraft.getInstance().getTextureManager().register(getUrlTextureId(url), texture);

        CACHE.put(url, texture);
    }
}
