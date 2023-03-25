package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.client.model.IGTCTMPredicate;
import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * @author KilaBash
 * @date 2023/3/24
 * @implNote BlockStateModelBlock
 */
public class RendererBlock extends Block implements IBlockRendererProvider, IGTCTMPredicate {
    public final IRenderer renderer;

    public RendererBlock(Properties properties, IRenderer renderer) {
        super(properties);
        this.renderer = renderer;
    }

    @Nullable
    @Override
    public IRenderer getRenderer(BlockState state) {
        return renderer;
    }

}
