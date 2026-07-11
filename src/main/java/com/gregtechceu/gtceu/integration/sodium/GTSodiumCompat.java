package com.gregtechceu.gtceu.integration.sodium;

import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.util.TextureMetadataHelper;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.TriState;

import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.parameters.AlphaCutoffParameter;
import net.caffeinemc.mods.sodium.client.render.texture.SpriteFinderCache;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class GTSodiumCompat {

    public static final TerrainRenderPass BLOOM_RENDER_PASS = new TerrainRenderPass(GTRenderTypes.bloom(), false, true);
    public static final Material BLOOM_MATERIAL = new Material(BLOOM_RENDER_PASS, AlphaCutoffParameter.ZERO, true);

    public static boolean quadHasBloom(QuadView quad, int[] ambientPackedLights, boolean emissive) {
        TextureAtlasSprite sprite = SpriteFinderCache.forBlockAtlas().find(quad);
        var metadata = TextureMetadataHelper.getMetadata(sprite);
        if (metadata.isPresent()) {
            TriState bloomValue = metadata.get().bloom();
            if (bloomValue == TriState.TRUE) return true;
            // Explicitly disable bloom if it's set to FALSE in the metadata
            else if (bloomValue == TriState.FALSE) return false;

            // fall through to emissivity config check if default
        }

        if (ConfigHolder.INSTANCE.client.bloom.emissiveTexturesHaveBloom) {
            return emissive || isEmissive(quad, ambientPackedLights);
        }

        return false;
    }

    public static boolean isEmissive(QuadView quad, int[] ambientPackedLights) {
        for (int i = 0; i < 4; i++) {
            int quadLight = quad.lightmap(i);
            int qBlock = LightTexture.block(quadLight), qSky = LightTexture.sky(quadLight);

            int ambientLight = ambientPackedLights[i];
            int aBlock = LightTexture.block(ambientLight), aSky = LightTexture.sky(ambientLight);

            if (qBlock > aBlock || qSky > aSky) {
                return true;
            }
        }
        return false;
    }
}
