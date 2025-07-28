package com.gregtechceu.gtceu.api.misc;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.CPacketImageRequest;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

public class ImageCache {

    private static final long REFRESH_MILLIS = 10000;
    private final Map<String, Pair<NativeImage, Long>> serverCache = new HashMap<>();
    private final Map<String, Pair<NativeImage, Long>> clientCache = new HashMap<>();
    private final Map<String, Consumer<NativeImage>> callbacks = new HashMap<>();
    @OnlyIn(Dist.CLIENT)
    private final Map<NativeImage, ResourceLocation> textures = new HashMap<>();
    private final Map<String, List<List<Byte>>> imageParts = new HashMap<>();
    private boolean downloading = false;

    public void getImage(String url, Consumer<NativeImage> callback, boolean isClient) {
        Map<String, Pair<NativeImage, Long>> cache = isClient ? clientCache : serverCache;
        if (cache.containsKey(url) && cache.get(url).getSecond() + REFRESH_MILLIS > Util.getEpochMillis()) {
            if (!isClient) GTCEu.LOGGER.info("Image found in cache, isClient = {}, timestamp = {}, url = {}", isClient,
                    cache.get(url).getSecond(), url);
            if (cache.get(url).getFirst() != null)
                callback.accept(cache.get(url).getFirst());
            return;
        }
        if (isClient) {
            cache.put(url, new Pair<>(null, Util.getEpochMillis()));
            callbacks.put(url, callback);
            GTCEu.LOGGER.info("Requesting image {}", url);
            GTNetwork.sendToServer(new CPacketImageRequest(url));
            return;
        }
        GTCEu.LOGGER.info("Received image request, downloading = {} for url {}", downloading, url);
        if (downloading) return;
        downloading = true;
        try {
            NativeImage image = NativeImage.read(new URL(url).openStream());
            cache.put(url, new Pair<>(image, Util.getEpochMillis()));
            GTCEu.LOGGER.info("Downloaded image {}! Executing callback", url);
            callback.accept(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        downloading = false;
    }

    @OnlyIn(Dist.CLIENT)
    public void putImage(String url, NativeImage image) {
        if (clientCache.get(url).getFirst() != null) {
            ResourceLocation texture = textures.get(clientCache.get(url).getFirst());
            if (texture != null) {
                Minecraft.getInstance().getTextureManager().release(texture);
            }
        }
        clientCache.put(url, new Pair<>(image, Util.getEpochMillis()));
        if (callbacks.get(url) != null) callbacks.get(url).accept(image);
    }

    @OnlyIn(Dist.CLIENT)
    public @Nullable ResourceLocation getTexture(String url) {
        getImage(url, image -> {}, true);
        if (clientCache.get(url) != null && clientCache.get(url).getFirst() != null) {
            NativeImage image = clientCache.get(url).getFirst();
            if (textures.get(image) != null) return textures.get(image);
            DynamicTexture texture = new DynamicTexture(image);
            ResourceLocation id = Minecraft.getInstance().getTextureManager()
                    .register("central_monitor_image_" + url.hashCode(), texture);
            textures.put(image, id);
            return id;
        } else return null;
    }

    public void receiveImagePart(String url, byte[] image, int index, int totalSize) throws IOException {
        if (!imageParts.containsKey(url)) imageParts.put(url, new ArrayList<>(totalSize));
        List<List<Byte>> parts = imageParts.get(url);
        while (parts.size() <= index) parts.add(new ArrayList<>());
        List<Byte> tmp = new ArrayList<>();
        for (byte i : image) tmp.add(i);
        parts.set(index, tmp);
        if (index == totalSize - 1) {
            List<Byte> fullImage = new ArrayList<>();
            for (List<Byte> part : parts) fullImage.addAll(part);
            byte[] byteArr = new byte[fullImage.size()];
            for (int i = 0; i < fullImage.size(); i++) byteArr[i] = fullImage.get(i);
            putImage(url, NativeImage.read(byteArr));
        }
    }
}
