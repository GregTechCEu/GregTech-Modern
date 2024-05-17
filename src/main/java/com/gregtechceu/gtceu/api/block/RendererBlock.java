package com.gregtechceu.gtceu.api.block;

import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

/**
 * @author KilaBash
 * @date 2023/3/24
 * @implNote BlockStateModelBlock
 */
public class RendererBlock extends AppearanceBlock implements IBlockRendererProvider {

    public final IRenderer renderer;

    public RendererBlock(Properties properties, IRenderer renderer) {
        super(properties);
        this.renderer = renderer;
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public IRenderer getRenderer(BlockState state) {
        return renderer;
    }
}
