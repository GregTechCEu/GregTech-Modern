package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.model.ctm.GTTextureMetadata;
import com.gregtechceu.gtceu.client.util.quad.transformers.GTQuadTransformers;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.TriState;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class TextureMetadataHelper {

    private static final Map<ResourceLocation, Optional<GTTextureMetadata>> metadataCache = new ConcurrentHashMap<>();

    public static Optional<GTTextureMetadata> getMetadata(ResourceLocation res) {
        return metadataCache.computeIfAbsent(res, loc -> {
            try {
                return Minecraft.getInstance().getResourceManager().getResource(res)
                        .flatMap(GTTextureMetadata::getForResourceUnsafe);
            } catch (Exception e) {
                // the real exception that's caught should always be an IOException,
                // but @SneakyThrows hides that from us so we catch all exceptions instead.
                GTCEu.LOGGER.error("Error loading metadata for location {}", res, e);

                return Optional.empty();
            }
        });
    }

    public static Optional<GTTextureMetadata> getMetadata(TextureAtlasSprite sprite) {
        return getMetadata(spriteToAbsolute(sprite.contents().name()));
    }

    public static Optional<GTTextureMetadata> getMetadata(Material material) {
        return getMetadata(spriteToAbsolute(material.texture()));
    }

    public static Optional<GTTextureMetadata> getMetadataFromRelativeLocation(ResourceLocation relativeLocation) {
        return getMetadata(spriteToAbsolute(relativeLocation));
    }

    public static ResourceLocation spriteToAbsolute(ResourceLocation sprite) {
        if (!sprite.getPath().startsWith("textures/")) {
            sprite = sprite.withPrefix("textures/");
        }
        if (!sprite.getPath().endsWith(".png")) {
            sprite = sprite.withSuffix(".png");
        }
        return sprite;
    }

    public static boolean hasBloom(BakedQuad quad, int[] ambientPackedLights) {
        var metadata = getMetadata(quad.getSprite());
        if (metadata.isPresent()) {
            TriState bloomValue = metadata.get().bloom();
            if (bloomValue == TriState.TRUE) return true;
            // Explicitly disable bloom if it's set to FALSE in the metadata
            else if (bloomValue == TriState.FALSE) return false;

            // fall through to emissivity config check if default
        }

        if (ConfigHolder.INSTANCE.client.bloom.emissiveTexturesHaveBloom) {
            return isEmissive(quad, ambientPackedLights);
        }

        return false;
    }

    public static boolean isEmissive(BakedQuad quad, int[] ambientPackedLights) {
        int[] quadPackedLights = GTQuadTransformers.getPackedLights(quad);

        for (int i = 0; i < 4; i++) {
            int quadLight = quadPackedLights[i];
            int qBlock = LightTexture.block(quadLight), qSky = LightTexture.sky(quadLight);

            int ambientLight = ambientPackedLights[i];
            int aBlock = LightTexture.block(ambientLight), aSky = LightTexture.sky(ambientLight);

            if (qBlock > aBlock || qSky > aSky) {
                return true;
            }
        }
        return false;
    }

    static void invalidateCaches() {
        metadataCache.clear();
    }
}
