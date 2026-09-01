package com.gregtechceu.gtceu.core.config;

import net.neoforged.fml.loading.FMLLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class RendererBackendCompatibility {

    public static final String SODIUM = "sodium";
    public static final String EMBEDDIUM = "embeddium";

    private static final Logger LOGGER = LogManager.getLogger("GregTechCEu/RendererBackendCompatibility");
    // TODO: Check compatibility with Sodium 0.8.13.
    private static final Set<String> SUPPORTED_SODIUM_VERSIONS = Set.of("0.8.12-beta.1+mc1.21.1");
    private static final Set<String> SUPPORTED_EMBEDDIUM_VERSIONS = Set.of("1.0.8-beta.367+mc1.21");
    private static final boolean ACCELERATED_BACKEND_LOADED = GTEarlyConfig.isModLoaded(SODIUM) ||
            GTEarlyConfig.isModLoaded(EMBEDDIUM);
    private static final @Nullable String ACTIVE_BACKEND = findActiveBackend();

    private RendererBackendCompatibility() {}

    public static boolean supports(String modId) {
        return modId.equals(ACTIVE_BACKEND);
    }

    public static boolean supportsCustomChunkPass() {
        return ACTIVE_BACKEND != null;
    }

    public static boolean requiresFallback() {
        return ACCELERATED_BACKEND_LOADED && ACTIVE_BACKEND == null;
    }

    private static @Nullable String findActiveBackend() {
        String sodiumVersion = getModVersion(SODIUM);
        String embeddiumVersion = getModVersion(EMBEDDIUM);

        if (sodiumVersion != null && embeddiumVersion != null) {
            LOGGER.warn("Both Sodium {} and Embeddium {} are loaded; disabling GTCEu renderer backend adapters",
                    sodiumVersion, embeddiumVersion);
            return null;
        }
        if (sodiumVersion != null) {
            return selectSupportedBackend(SODIUM, sodiumVersion, SUPPORTED_SODIUM_VERSIONS);
        }
        if (embeddiumVersion != null) {
            return selectSupportedBackend(EMBEDDIUM, embeddiumVersion, SUPPORTED_EMBEDDIUM_VERSIONS);
        }
        return null;
    }

    private static @Nullable String selectSupportedBackend(String modId, String version,
                                                           Set<String> supportedVersions) {
        if (supportedVersions.contains(version)) {
            return modId;
        }

        LOGGER.warn("Unsupported {} version {}; disabling GTCEu renderer backend adapters", modId, version);
        return null;
    }

    private static @Nullable String getModVersion(String modId) {
        return FMLLoader.getLoadingModList().getMods().stream()
                .filter(mod -> modId.equals(mod.getModId()))
                .findFirst()
                .map(mod -> mod.getVersion().toString())
                .orElse(null);
    }
}
