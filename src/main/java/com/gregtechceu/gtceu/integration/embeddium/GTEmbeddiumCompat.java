package com.gregtechceu.gtceu.integration.embeddium;

import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;

import lombok.Getter;
import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.embeddedt.embeddium.impl.render.chunk.terrain.material.Material;
import org.embeddedt.embeddium.impl.render.chunk.terrain.material.parameters.AlphaCutoffParameter;

public class GTEmbeddiumCompat {

    @Getter(lazy = true)
    private static final TerrainRenderPass bloomRenderPass = new TerrainRenderPass(GTRenderTypes.bloom(), false, true);
    @Getter(lazy = true)
    private static final Material bloomMaterial = new Material(getBloomRenderPass(), AlphaCutoffParameter.ONE_TENTH,
            true);
}
