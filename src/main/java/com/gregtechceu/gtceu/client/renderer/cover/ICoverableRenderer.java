package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/24
 * @implNote IMachineRenderer
 */
public interface ICoverableRenderer {

    @OnlyIn(Dist.CLIENT)
    default List<BakedQuad> renderModel(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction side,
                                        RandomSource rand) {
        var blockEntity = level == null ? null : level.getBlockEntity(pos);
        if (blockEntity != null) {
            var coverable = GTCapabilityHelper.getCoverable(blockEntity.getLevel(), blockEntity.getBlockPos(), null);
            if (coverable != null) {
                var quads = new LinkedList<BakedQuad>();
                var modelState = ModelFactory.getRotation(coverable.getFrontFacing());
                var modelFacing = side == null ? null : ModelFactory.modelFacing(side, coverable.getFrontFacing());
                renderCovers(quads, side, rand, coverable, modelFacing, pos, level, modelState);
                return quads;
            }
        }
        return Collections.emptyList();
    }

    @OnlyIn(Dist.CLIENT)
    default void renderCovers(List<BakedQuad> quads, @Nullable Direction side, RandomSource rand,
                              @NotNull ICoverable coverable, @Nullable Direction modelFacing, BlockPos pos,
                              BlockAndTintGetter level, ModelState modelState) {
        var thickness = coverable.getCoverPlateThickness();
        for (Direction face : GTUtil.DIRECTIONS) {
            var cover = coverable.getCoverAtSide(face);
            if (cover != null) {
                if (thickness > 0 && cover.shouldRenderPlate()) {
                    double min = thickness;
                    double max = 1d - thickness;
                    var normal = face.getNormal();
                    var cube = new AABB(
                            normal.getX() == 0 ? 0.001 : normal.getX() > 0 ? max : 0.001,
                            normal.getY() == 0 ? 0.001 : normal.getY() > 0 ? max : 0.001,
                            normal.getZ() == 0 ? 0.001 : normal.getZ() > 0 ? max : 0.001,
                            normal.getX() == 0 ? 0.999 : normal.getX() > 0 ? 0.999 : min,
                            normal.getY() == 0 ? 0.999 : normal.getY() > 0 ? 0.999 : min,
                            normal.getZ() == 0 ? 0.999 : normal.getZ() > 0 ? 0.999 : min);
                    if (side == null) { // render back
                        quads.add(FaceQuad
                                .builder(face.getOpposite(),
                                        ModelFactory.getBlockSprite(GTCEu.id("block/material_sets/dull/wire_side")))
                                .cube(cube).cubeUV().tintIndex(-1).bake());
                    } else if (side != face.getOpposite()) { // render sides
                        quads.add(FaceQuad
                                .builder(side,
                                        ModelFactory.getBlockSprite(GTCEu.id("block/material_sets/dull/wire_side")))
                                .cube(cube).cubeUV().tintIndex(-1).bake());
                    }
                }
                cover.getCoverRenderer().renderCover(quads, side, rand, cover, modelFacing, pos, level, modelState);
            }
        }
    }
}
