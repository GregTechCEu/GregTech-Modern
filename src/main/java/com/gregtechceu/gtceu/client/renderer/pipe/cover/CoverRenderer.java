package com.gregtechceu.gtceu.client.renderer.pipe.cover;

import com.gregtechceu.gtceu.client.renderer.pipe.util.ColorData;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import java.util.EnumSet;
import java.util.List;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface CoverRenderer {

    void addQuads(List<BakedQuad> quads, Direction side, RandomSource rand, EnumSet<Direction> renderPlate,
                  boolean renderBackside,
                  ModelData modelData, ColorData data, RenderType renderType);
}
