package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ICoverRenderer {

    @OnlyIn(Dist.CLIENT)
    void renderCover(List<BakedQuad> quads, @Nullable Direction side, RandomSource rand,
                     CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                     ModelData modelData, @Nullable RenderType renderType);

    default ChunkRenderTypeSet getRenderTypes(CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                                              RandomSource rand, ModelData modelData) {
        return ChunkRenderTypeSet.of(RenderType.cutoutMipped());
    }

    default ModelData getModelData(CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                                   ModelData holderModelData) {
        return ModelData.EMPTY;
    }

    default boolean shouldRenderBackPlateForSide(CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                                                 @Nullable Direction side) {
        return true;
    }
}
