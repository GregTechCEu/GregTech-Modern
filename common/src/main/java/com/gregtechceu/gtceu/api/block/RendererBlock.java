package com.gregtechceu.gtceu.api.block;

import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.state.BlockState;
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
    @Environment(EnvType.CLIENT)
    public IRenderer getRenderer(BlockState state) {
        return renderer;
    }

}
