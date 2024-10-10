package com.gregtechceu.gtceu.client.model;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public record GTMetadataSection(boolean bloom) {

    public static final String SECTION_NAME = GTCEu.MOD_ID;
    public static final GTMetadataSection MISSING = new GTMetadataSection(false);

    @Nullable
    public static GTMetadataSection getMetadata(ResourceLocation res) {
        GTMetadataSection ret = MISSING;
        try {
            var resource = Minecraft.getInstance().getResourceManager().getResource(res);
            if (resource.isPresent()) {
                ret = resource.get().metadata().getSection(GTMetadataSection.Serializer.INSTANCE).orElse(MISSING);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ret;
    }

    public static boolean hasBloom(TextureAtlasSprite sprite) {
        if (sprite == null) return false;
        // noinspection resource
        GTMetadataSection ret = getMetadata(spriteToAbsolute(sprite.contents().name()));
        return ret != null && ret.bloom;
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

    public static class Serializer implements MetadataSectionSerializer<GTMetadataSection> {

        static GTMetadataSection.Serializer INSTANCE = new GTMetadataSection.Serializer();

        @NotNull
        @Override
        public String getMetadataSectionName() {
            return SECTION_NAME;
        }

        @Override
        public GTMetadataSection fromJson(JsonObject json) {
            boolean bloom = false;
            if (json.isJsonObject()) {
                JsonObject obj = json.getAsJsonObject();
                if (obj.has("bloom")) {
                    JsonElement element = obj.get("bloom");
                    if (element.isJsonPrimitive() &&
                            element.getAsJsonPrimitive().isBoolean()) {
                        bloom = element.getAsBoolean();
                    }
                }
            }
            return new GTMetadataSection(bloom);
        }
    }
}
