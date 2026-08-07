package com.gregtechceu.gtceu.api.misc;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ImageCache {

    public static final long REFRESH_SECS = 120;
    public static final long EXPIRE_SECS = 300;
    private static final byte[] NULL_MARKER = new byte[0];
    private static final String[] ALLOWED_PROTOCOLS = new String[] { "http", "https" };

    private static boolean downloading = false;

    private static final LoadingCache<String, byte[]> CACHE = CacheBuilder.newBuilder()
            .refreshAfterWrite(REFRESH_SECS, TimeUnit.SECONDS)
            .expireAfterAccess(EXPIRE_SECS, TimeUnit.SECONDS)
            .concurrencyLevel(3)
            .build(CacheLoader.from(urlString -> {
                try {
                    URL url = new URL(urlString);
                    boolean singleplayer = GTCEu.getMinecraftServer().isSingleplayer() &&
                            !GTCEu.getMinecraftServer().isPublished();
                    boolean allowedProtocol = singleplayer;
                    for (String protocol : ALLOWED_PROTOCOLS) {
                        if (url.getProtocol().equalsIgnoreCase(protocol)) {
                            allowedProtocol = true;
                            break;
                        }
                    }
                    if (!allowedProtocol) return NULL_MARKER;
                    boolean allowedDomain = singleplayer;
                    for (String domain : ConfigHolder.INSTANCE.gameplay.allowedImageDomains) {
                        if (url.getHost().equalsIgnoreCase(domain)) {
                            allowedDomain = true;
                            break;
                        }
                    }
                    if (!allowedDomain) return NULL_MARKER;
                    if (downloading) return NULL_MARKER;
                    downloading = true;

                    try (InputStream stream = url.openStream()) {
                        byte[] image = stream.readAllBytes();
                        GTCEu.LOGGER.debug("Downloaded image {}! Executing callback", url);
                        return image;
                    } catch (IOException e) {
                        GTCEu.LOGGER.error("Could not load image {}", url, e);
                        return NULL_MARKER;
                    } finally {
                        downloading = false;
                    }
                } catch (MalformedURLException e) {
                    return NULL_MARKER;
                }
            }));

    public static void queryServerImage(String url, Consumer<byte[]> callback) {
        try {
            if (downloading) return;

            byte[] image = CACHE.get(url);
            if (image != NULL_MARKER) {
                callback.accept(image);
            } else {
                CACHE.invalidate(url);
            }
        } catch (ExecutionException e) {
            Throwable t = e;
            if (t.getCause() != null) {
                t = t.getCause();
            }
            GTCEu.LOGGER.error("Could not load image {}", url, t);
        }
    }
}
