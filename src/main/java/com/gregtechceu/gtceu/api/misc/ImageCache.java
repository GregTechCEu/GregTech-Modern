package com.gregtechceu.gtceu.api.misc;

import com.gregtechceu.gtceu.GTCEu;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ImageCache {

    public static final long REFRESH_SECS = 120;
    public static final long EXPIRE_SECS = 300;
    private static final byte[] NULL_MARKER = new byte[0];

    private static boolean downloading = false;

    private static final LoadingCache<String, byte[]> CACHE = CacheBuilder.newBuilder()
            .refreshAfterWrite(REFRESH_SECS, TimeUnit.SECONDS)
            .expireAfterAccess(EXPIRE_SECS, TimeUnit.SECONDS)
            .concurrencyLevel(3)
            .build(CacheLoader.from(url -> {
                if (downloading) return NULL_MARKER;
                downloading = true;

                try (InputStream stream = new URL(url).openStream()) {
                    return stream.readAllBytes();
                } catch (IOException e) {
                    GTCEu.LOGGER.error("Could not load image {}", url, e);
                    downloading = false;
                    return NULL_MARKER;
                } finally {
                    GTCEu.LOGGER.debug("Downloaded image {}! Executing callback", url);
                    downloading = false;
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
