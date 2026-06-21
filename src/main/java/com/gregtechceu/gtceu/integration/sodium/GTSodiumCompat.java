package com.gregtechceu.gtceu.integration.sodium;

import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.util.TextureMetadataHelper;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.TriState;

import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.parameters.AlphaCutoffParameter;
import net.minecraft.client.renderer.LightTexture;

public class GTSodiumCompat {

    public static final TerrainRenderPass BLOOM_RENDER_PASS = new TerrainRenderPass(GTRenderTypes.bloom(), false, true);
    public static final Material BLOOM_MATERIAL = new Material(BLOOM_RENDER_PASS, AlphaCutoffParameter.ZERO, true);

    public static boolean quadHasBloom(ModelQuadView quad, int[] ambientPackedLights, boolean emissive) {
        var metadata = TextureMetadataHelper.getMetadata(quad.getSprite());
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

    public static boolean isEmissive(ModelQuadView quad, int[] ambientPackedLights) {
        for (int i = 0; i < 4; i++) {
            int quadLight = quad.getLight(i);
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
