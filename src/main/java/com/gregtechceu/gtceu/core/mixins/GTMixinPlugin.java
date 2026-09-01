package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.config.GTEarlyConfig;
import com.gregtechceu.gtceu.core.config.Option;
import com.gregtechceu.gtceu.core.config.RendererBackendCompatibility;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.UnknownNullability;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.File;
import java.util.*;

import static com.gregtechceu.gtceu.core.config.GTEarlyConfig.OPTIFINE_PRESENT;

public class GTMixinPlugin implements IMixinConfigPlugin {

    public static final Logger LOGGER = LogManager.getLogger("GregTechCEu");

    public static final String MIXIN_PACKAGE_ROOT = "com.gregtechceu.gtceu.core.mixins.";

    public static @UnknownNullability GTEarlyConfig CONFIG = null;

    public GTMixinPlugin() {
        if (CONFIG != null) {
            return;
        }

        try {
            CONFIG = GTEarlyConfig.load(new File("./config/gtceu-early.properties"));
        } catch (Exception e) {
            throw new RuntimeException("Could not load mixin configuration file for GTCEu", e);
        }

        if (OPTIFINE_PRESENT) {
            LOGGER.fatal(
                    "OptiFine detected. Use of GTCEu with OptiFine is not supported due to its breakage of NeoForge features.");
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.startsWith(MIXIN_PACKAGE_ROOT)) {
            LOGGER.error("Expected mixin '{}' to start with package root '{}', treating as foreign and disabling!",
                    mixinClassName, MIXIN_PACKAGE_ROOT);

            return false;
        }

        String mixin = mixinClassName.substring(MIXIN_PACKAGE_ROOT.length());

        Boolean rendererOverride = shouldApplyRendererMixin(mixin);
        if (rendererOverride != null) {
            return rendererOverride;
        }

        if (!isOptionEnabled(mixin)) {
            return false;
        }

        return true;
    }

    private static Boolean shouldApplyRendererMixin(String mixin) {
        boolean bloomSafeMode = isOptionEnabled(GTEarlyConfig.SAFE_MODE);
        boolean layerSafeMode = isOptionEnabled(GTEarlyConfig.CUSTOM_CHUNK_LAYER_SAFE_MODE);

        if (mixin.startsWith("client.customchunk.sodium.")) {
            return !layerSafeMode && RendererBackendCompatibility.supports(RendererBackendCompatibility.SODIUM);
        }
        if (mixin.startsWith("client.bloom.normal.sodium.")) {
            return !bloomSafeMode && !layerSafeMode &&
                    RendererBackendCompatibility.supports(RendererBackendCompatibility.SODIUM);
        }
        if (mixin.startsWith("client.bloom.safemode.sodium.")) {
            return (bloomSafeMode || layerSafeMode) &&
                    RendererBackendCompatibility.supports(RendererBackendCompatibility.SODIUM);
        }
        if (mixin.startsWith("client.customchunk.embeddium.")) {
            return !layerSafeMode && RendererBackendCompatibility.supports(RendererBackendCompatibility.EMBEDDIUM);
        }
        if (mixin.startsWith("client.bloom.normal.embeddium.")) {
            return !bloomSafeMode && !layerSafeMode &&
                    RendererBackendCompatibility.supports(RendererBackendCompatibility.EMBEDDIUM);
        }
        if (mixin.startsWith("client.bloom.safemode.embeddium.")) {
            return (bloomSafeMode || layerSafeMode) &&
                    RendererBackendCompatibility.supports(RendererBackendCompatibility.EMBEDDIUM);
        }
        if (mixin.startsWith("client.customchunk.iris.")) {
            return !layerSafeMode && RendererBackendCompatibility.supportsCustomChunkPass();
        }
        return null;
    }

    public static boolean isOptionEnabled(String mixin) {
        Option option = CONFIG.getEffectiveOptionForMixin(mixin);
        if (option == null) {
            // if the mixin doesn't have an option, it's always enabled
            return true;
        }

        return option.isEnabled();
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
