package com.gregtechceu.gtceu.client.model.fabric;

import com.gregtechceu.gtlib.client.model.fabric.GTLibRendererModel;
import com.gregtechceu.gtlib.client.renderer.IBlockRendererProvider;
import com.gregtechceu.gtlib.client.renderer.IRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/27
 * @implNote ModelUtilImpl
 */
public class ModelUtilImpl {
    public static List<BakedQuad> getBakedModelQuads(BakedModel model, BlockAndTintGetter level, BlockPos pos, BlockState state, Direction side, RandomSource rand) {
        if (model instanceof GTLibRendererModel.RendererBakedModel) {
            if (state.getBlock() instanceof IBlockRendererProvider rendererProvider) {
                IRenderer renderer = rendererProvider.getRenderer(state);
                if (renderer != null) {
                    return renderer.renderModel(level, pos, state, side, rand);
                }
            }
        }
        return model.getQuads(state, side, rand);
    }
}
