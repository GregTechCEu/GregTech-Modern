package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.model.ctm.GTTextureMetadata;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@UtilityClass
public class TextureMetadataHelper {

    private static final Map<ResourceLocation, @Nullable GTTextureMetadata> metadataCache = new HashMap<>();

    public static Optional<GTTextureMetadata> getMetadata(ResourceLocation res) {
        // Note, semantically different from computeIfAbsent, as we DO care about keys mapped to null values
        if (metadataCache.containsKey(res)) {
            return Optional.ofNullable(metadataCache.get(res));
        }
        Optional<GTTextureMetadata> ret;
        try {
            ret = Minecraft.getInstance().getResourceManager().getResource(res)
                    .flatMap(GTTextureMetadata::getForResourceUnsafe);
        } catch (Exception e) {
            // the real exception that's caught should always be an IOException,
            // but @SneakyThrows hides that from us so we catch all exceptions instead.
            ret = Optional.empty();
            GTCEu.LOGGER.error("Error loading metadata for location {}", res, e);
        }
        ret.ifPresentOrElse(r -> metadataCache.put(res, r), () -> metadataCache.put(res, null));
        return ret;
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

    static void invalidateCaches() {
        metadataCache.clear();
    }
}
