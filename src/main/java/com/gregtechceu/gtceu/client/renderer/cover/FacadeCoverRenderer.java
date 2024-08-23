package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.client.model.ModelUtil;
import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRenderer;
import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRendererBuilder;
import com.gregtechceu.gtceu.common.cover.FacadeCover;
import com.gregtechceu.gtceu.common.item.FacadeItemBehaviour;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.utils.FacadeBlockAndTintGetter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4d;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/24
 * @implNote FacadeRenderer
 *           It can only be used for item.
 *           call it in other renderer to render a facade cover.
 */
public class FacadeCoverRenderer implements ICoverRenderer {

    public final static FacadeCoverRenderer INSTANCE = new FacadeCoverRenderer();

    protected FacadeCoverRenderer() {}

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean useBlockLight(ItemStack stack) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, boolean leftHand, PoseStack matrixStack,
                           MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model) {
        var mc = Minecraft.getInstance();
        var renderItem = FacadeItemBehaviour.getFacadeStack(stack);
        BlockState blockState = null;
        if (renderItem.getItem() instanceof BlockItem blockItem) {
            blockState = blockItem.getBlock().defaultBlockState();
        }
        if (blockState != null && mc.level != null) {
            model = mc.getBlockRenderer().getBlockModel(blockState);
            if (!model.isCustomRenderer()) {
                matrixStack.pushPose();
                ModelFactory.MODEL_TRANSFORM_BLOCK.getTransform(transformType).apply(leftHand, matrixStack);
                matrixStack.translate(0, -0.1D, -0.5D);
                if (transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ||
                        transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
                    matrixStack.translate(0.5, 0.5, 0.5);

                    matrixStack.mulPose(new Quaternionf(new AxisAngle4d().set(90, 0, 1, 0)));
                    matrixStack.translate(-0.5, -0.5, -0.5);
                }
                var pose = matrixStack.last();

                var level = new FacadeBlockAndTintGetter(mc.level, BlockPos.ZERO, blockState, null);
                var quads = new LinkedList<>(ModelUtil.getBakedModelQuads(model, level, BlockPos.ZERO, blockState,
                        Direction.NORTH, mc.level.random));

                var cube = new AABB(0.01, 0.01, 0.01, 0.99, 0.99, 1 / 16f);

                for (Direction side : GTUtil.DIRECTIONS) {
                    if (side != Direction.NORTH) {
                        quads.add(FaceQuad.builder(side, ModelFactory.getBlockSprite(GTCEu.id("block/cable/wire")))
                                .cube(cube).cubeUV().tintIndex(-1).bake());
                        quads.add(FaceQuad.builder(side, ModelFactory.getBlockSprite(GTCEu.id("block/cable/wire")))
                                .cube(cube).cubeUV().tintIndex(-1).bake());
                    }
                }

                for (BakedQuad bakedQuad : quads) {
                    buffer.getBuffer(RenderType.cutout()).putBulkData(pose, bakedQuad, 1, 1, 1, combinedLight,
                            combinedOverlay);
                }

                matrixStack.popPose();
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderCover(List<BakedQuad> quads, Direction side, RandomSource rand,
                            @NotNull CoverBehavior coverBehavior, Direction modelFacing, BlockPos pos,
                            BlockAndTintGetter level, ModelState modelState) {
        if (coverBehavior instanceof FacadeCover facadeCover) {
            var state = facadeCover.getFacadeState();
            if (state.getRenderShape() == RenderShape.MODEL) {
                BlockRenderDispatcher brd = Minecraft.getInstance().getBlockRenderer();
                BakedModel model = brd.getBlockModel(state);
                if (side == coverBehavior.attachedSide) {
                    quads.addAll(ModelUtil.getBakedModelQuads(model, level, pos, state, side, rand));
                } else if (side == null && coverBehavior.coverHolder.shouldRenderBackSide()) {
                    var normal = coverBehavior.attachedSide.getNormal();
                    var cube = new AABB(
                            normal.getX() == 0 ? 0 : normal.getX() > 0 ? 1 : 0,
                            normal.getY() == 0 ? 0 : normal.getY() > 0 ? 1 : 0,
                            normal.getZ() == 0 ? 0 : normal.getZ() > 0 ? 1 : 0,
                            normal.getX() == 0 ? 1 : normal.getX() > 0 ? 1 : 0,
                            normal.getY() == 0 ? 1 : normal.getY() > 0 ? 1 : 0,
                            normal.getZ() == 0 ? 1 : normal.getZ() > 0 ? 1 : 0);
                    for (BakedQuad quad : ModelUtil.getBakedModelQuads(model, level, pos, state,
                            coverBehavior.attachedSide, rand)) {
                        quads.add(FaceQuad.builder(coverBehavior.attachedSide.getOpposite(), quad.getSprite())
                                .cube(cube)
                                .shade(quad.isShade())
                                .tintIndex(quad.getTintIndex())
                                .bake());
                    }
                }
            }
        }
    }

    public static CoverRenderer createRenderer(final BlockAndTintGetter world, BlockPos pos, final BlockState state) {
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

        BakedModel model = dispatcher.getBlockModel(state);

        return (quads, side, rand, renderPlate, renderBackside, modelData, data, renderType) -> {
            if (renderType != RenderType.cutoutMipped()) return;

            AABB cube = CoverRendererBuilder.PLATE_AABBS.get(side);
            for (BakedQuad quad : ModelUtil.getBakedModelQuads(model, world, pos, state, side, rand)) {
                quads.add(FaceQuad.builder(side.getOpposite(), quad.getSprite())
                        .cube(cube)
                        .shade(quad.isShade())
                        .tintIndex(quad.getTintIndex())
                        .bake());
            }
        };
    }
}
