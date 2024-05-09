package com.gregtechceu.gtceu.api.blocks;

import com.gregtechceu.gtceu.api.materials.material.Material;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public class RendererMaterialBlock extends MaterialBlock implements IBlockRendererProvider {
    public final IRenderer renderer;

    public RendererMaterialBlock(Properties properties, TagPrefix tagPrefix, Material material, @Nullable IRenderer renderer) {
        super(properties, tagPrefix, material, false);
        this.renderer = renderer;
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public IRenderer getRenderer(BlockState state) {
        return renderer;
    }
}
