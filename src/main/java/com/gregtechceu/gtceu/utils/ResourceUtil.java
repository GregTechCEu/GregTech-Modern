package com.gregtechceu.gtceu.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import com.google.gson.JsonParseException;
import lombok.experimental.UtilityClass;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

@UtilityClass
public class ResourceUtil {
    
    public static Resource getResource(TextureAtlasSprite sprite) throws IOException {
        return getResource(spriteToAbsolute(sprite.contents().name()));
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
    
    public static Resource getResource(ResourceLocation res) throws FileNotFoundException {
        return Minecraft.getInstance().getResourceManager().getResourceOrThrow(res);
    }
    
    public static Resource getResourceUnsafe(ResourceLocation res) {
        try {
            return getResource(res);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static final Map<ResourceLocation, IMetadataSectionCTM> metadataCache = new HashMap<>();
    private static final IMetadataSectionCTM.Serializer SERIALIZER = new IMetadataSectionCTM.Serializer();

    public static Optional<IMetadataSectionCTM> getMetadata(ResourceLocation res) throws IOException {
        // Note, semantically different from computeIfAbsent, as we DO care about keys mapped to null values
        if (metadataCache.containsKey(res)) {
            return Optional.ofNullable(metadataCache.get(res));
        }
        Optional<IMetadataSectionCTM> ret;
        try {
            Resource resource = getResource(res);
            ret = resource.metadata().getSection(SERIALIZER);
        } catch (FileNotFoundException e) {
            ret = Optional.empty();
        } catch (JsonParseException e) {
            throw new IOException("Error loading metadata for location " + res, e);
        }
        ret.ifPresentOrElse(r -> metadataCache.put(res, r), () -> metadataCache.put(res, null));
        return ret;
    }
    
    public static Optional<IMetadataSectionCTM> getMetadata(TextureAtlasSprite sprite) throws IOException {
        return getMetadata(spriteToAbsolute(sprite.contents().name()));
    }
    
    public static Optional<IMetadataSectionCTM> getMetadataUnsafe(TextureAtlasSprite sprite) {
        try {
            return getMetadata(sprite);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void invalidateCaches() {
        metadataCache.clear();
    }
}