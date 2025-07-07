package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public interface ICoverableRenderer {

    @OnlyIn(Dist.CLIENT)
    TextureAtlasSprite[] COVER_BACK_PLATE = new TextureAtlasSprite[1];

    @OnlyIn(Dist.CLIENT)
    static void initSprites(Function<ResourceLocation, TextureAtlasSprite> atlas) {
        COVER_BACK_PLATE[0] = atlas.apply(GTCEu.id("block/material_sets/dull/wire_side"));
    }

    @OnlyIn(Dist.CLIENT)
    default void renderCovers(List<BakedQuad> quads, @NotNull ICoverable coverable,
                              BlockPos pos, BlockAndTintGetter level, @Nullable Direction side,
                              RandomSource rand, @NotNull ModelData modelData, @Nullable RenderType renderType) {
        var thickness = coverable.getCoverPlateThickness();
        for (Direction face : GTUtil.DIRECTIONS) {
            var cover = coverable.getCoverAtSide(face);
            if (cover != null) {
                if (thickness > 0 && cover.shouldRenderPlate()) {
                    double min = thickness;
                    double max = 1d - thickness;
                    var normal = face.getNormal();
                    var cube = new AABB(
                            normal.getX() > 0 ? max : 0.001,
                            normal.getY() > 0 ? max : 0.001,
                            normal.getZ() > 0 ? max : 0.001,
                            normal.getX() >= 0 ? 0.999 : min,
                            normal.getY() >= 0 ? 0.999 : min,
                            normal.getZ() >= 0 ? 0.999 : min);
                    if (side == null) { // render back
                        quads.add(FaceQuad.builder(face.getOpposite(), COVER_BACK_PLATE[0])
                                .cube(cube).cubeUV().tintIndex(-1).bake());
                    } else if (side != face.getOpposite()) { // render sides
                        quads.add(FaceQuad.builder(side, COVER_BACK_PLATE[0])
                                .cube(cube).cubeUV().tintIndex(-1).bake());
                    }
                }
                // it won't ever be null on the client
                // noinspection DataFlowIssue
                cover.getCoverRenderer().get()
                        .renderCover(quads, side, rand, cover, pos, level, modelData, renderType);
            }
        }
    }
}
