package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

public class RendererMaterialBlock extends MaterialBlock {

    public final IRenderer renderer;

    public RendererMaterialBlock(Properties properties, TagPrefix tagPrefix, Material material,
                                 @Nullable IRenderer renderer) {
        super(properties, tagPrefix, material, false);
        this.renderer = renderer;
    }

    @OnlyIn(Dist.CLIENT)
    public @Nullable IRenderer getRenderer() {
        return renderer;
    }
}
