package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/24
 * @implNote ICoverRenderer
 * Do not use it as a block renderer alone. It should be called from {@link ICoverableRenderer}
 */
public interface ICoverRenderer extends IRenderer {

    /**
     * Use {@link #renderCover(List, Direction, RandomSource, CoverBehavior, Direction, ModelState)} instead
     */
    @Override
    @Deprecated
    @Environment(EnvType.CLIENT)
    default List<BakedQuad> renderModel(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction side, RandomSource rand) {
        return IRenderer.super.renderModel(level, pos, state, side, rand);
    }

    @Environment(EnvType.CLIENT)
    void renderCover(List<BakedQuad> quads, @Nullable Direction side, RandomSource rand, @NotNull CoverBehavior coverBehavior, @Nullable Direction modelFacing, ModelState modelState);
}
