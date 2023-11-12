package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RendererMaterialBlock extends MaterialBlock implements IBlockRendererProvider {
    public final IRenderer renderer;

    public RendererMaterialBlock(Properties properties, TagPrefix tagPrefix, Material material, @Nullable IRenderer renderer) {
        super(properties, tagPrefix, material, false);
        this.renderer = renderer;
    }

    @Nullable
    @Override
    @Environment(EnvType.CLIENT)
    public IRenderer getRenderer(BlockState state) {
        return renderer;
    }
}
