package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.core.config.GTEarlyConfig;
import com.gregtechceu.gtceu.core.config.RendererBackendCompatibility;
import com.gregtechceu.gtceu.core.mixins.GTMixinPlugin;

public final class CustomChunkRenderBackend {

    public static boolean isEnabled() {
        return !isSafeModeEnabled() && RendererBackendCompatibility.supportsCustomChunkPass();
    }

    public static boolean isSafeModeEnabled() {
        return GTMixinPlugin.isOptionEnabled(GTEarlyConfig.CUSTOM_CHUNK_LAYER_SAFE_MODE);
    }

    private CustomChunkRenderBackend() {}
}
