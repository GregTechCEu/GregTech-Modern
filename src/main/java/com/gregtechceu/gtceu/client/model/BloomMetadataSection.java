package com.gregtechceu.gtceu.client.model;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.util.GTQuadTransformers;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

import java.io.IOException;

public record BloomMetadataSection(boolean bloom) {

    public static final String SECTION_NAME = GTCEu.MOD_ID;

    public static final Object2BooleanMap<ResourceLocation> KNOWN_BLOOM_TEXTURES = new Object2BooleanOpenHashMap<>();

    public static boolean hasBloom(TextureAtlasSprite sprite) {
        ResourceLocation textureLoc = SpriteSource.TEXTURE_ID_CONVERTER.idToFile(sprite.contents().name());
        return hasBloom(textureLoc);
    }

    public static boolean hasBloom(ResourceLocation res) {
        return KNOWN_BLOOM_TEXTURES.computeIfAbsent(res, loc -> {
            try {
                var resource = Minecraft.getInstance().getResourceManager().getResource(res);
                if (resource.isPresent()) {
                    return resource.get().metadata()
                            .getSection(Serializer.INSTANCE).map(BloomMetadataSection::bloom)
                            .orElse(false);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return false;
        });
    }

    public static boolean hasBloom(BakedQuad quad, int[] ambientPackedLights) {
        if (!quad.isShade() || !quad.hasAmbientOcclusion()) {
            return true;
        }
        if (hasBloom(quad.getSprite())) {
            return true;
        }
        return ConfigHolder.INSTANCE.client.shader.emissiveTexturesHaveBloom && isEmissive(quad, ambientPackedLights);
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

    public static class Serializer implements MetadataSectionSerializer<BloomMetadataSection> {

        static BloomMetadataSection.Serializer INSTANCE = new BloomMetadataSection.Serializer();

        @Override
        public String getMetadataSectionName() {
            return SECTION_NAME;
        }

        @Override
        public BloomMetadataSection fromJson(JsonObject json) {
            return new BloomMetadataSection(GsonHelper.getAsBoolean(json, "bloom"));
        }
    }
}
