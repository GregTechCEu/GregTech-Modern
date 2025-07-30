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
import net.minecraftforge.client.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ICoverRenderer {

    @OnlyIn(Dist.CLIENT)
    void renderCover(List<BakedQuad> quads, @Nullable Direction side, RandomSource rand,
                     @NotNull CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                     @NotNull ModelData modelData, @Nullable RenderType renderType);
}
