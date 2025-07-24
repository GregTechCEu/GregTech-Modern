package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ICoverableRenderer {

    @OnlyIn(Dist.CLIENT)
    TextureAtlasSprite[] COVER_BACK_PLATE = new TextureAtlasSprite[1];

    @OnlyIn(Dist.CLIENT)
    static void initSprites(TextureAtlas atlas) {
        COVER_BACK_PLATE[0] = atlas.getSprite(GTCEu.id("block/material_sets/dull/wire_side"));
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
                    double min = thickness + 0.01;
                    double max = 0.99 - thickness;
                    var normal = face.getNormal();
                    var cube = new AABB(
                            normal.getX() > 0 ? max : 0.01,
                            normal.getY() > 0 ? max : 0.01,
                            normal.getZ() > 0 ? max : 0.01,
                            normal.getX() >= 0 ? 0.99 : min,
                            normal.getY() >= 0 ? 0.99 : min,
                            normal.getZ() >= 0 ? 0.99 : min);
                    if (side == null) { // render back
                        quads.add(FaceQuad.builder(face.getOpposite(), COVER_BACK_PLATE[0])
                                .cube(cube).cubeUV().bake());
                    } else if (side != face.getOpposite()) { // render sides
                        quads.add(FaceQuad.builder(side, COVER_BACK_PLATE[0])
                                .cube(cube).cubeUV().bake());
                    }
                }
                // it won't ever be null on the client
                // noinspection DataFlowIssue
                cover.getCoverRenderer().get()
                        .renderCover(quads, side, rand, cover, pos, level, modelData, renderType);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    default void renderDynamicCovers(MetaMachine machine, float partialTick, PoseStack poseStack,
                                     MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ICoverable coverable = machine.getCoverContainer();
        for (Direction face : GTUtil.DIRECTIONS) {
            CoverBehavior cover = coverable.getCoverAtSide(face);
            IDynamicCoverRenderer renderer = cover != null ? cover.getDynamicRenderer().get() : null;
            if (renderer != null) {
                poseStack.pushPose();
                RenderUtil.moveToFace(poseStack, .5f, .5f, .5f, face);
                RenderUtil.rotateToFace(poseStack, face, Direction.NORTH);
                poseStack.translate(-.5f, -.5f, .01f);
                renderer.render(machine, face, partialTick, poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();
            }
        }
    }
}
