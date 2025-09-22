package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.client.model.IBlockEntityRendererBakedModel;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("unchecked")
@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockEntityWithBERModelRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {

    private final BlockRenderDispatcher blockRenderDispatcher;

    public BlockEntityWithBERModelRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderDispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(T blockEntity, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        BlockState blockState = blockEntity.getBlockState();
        BakedModel model = blockRenderDispatcher.getBlockModel(blockState);

        if (model instanceof IBlockEntityRendererBakedModel<?> berModel) {
            if (berModel.getBlockEntityType() != blockEntity.getType()) return;

            ((IBlockEntityRendererBakedModel<T>) berModel).render(blockEntity, partialTick,
                    poseStack, buffer, packedLight, packedOverlay);
        } else {
            Level level = blockEntity.getLevel();
            BlockPos pos = blockEntity.getBlockPos();

            ModelData modelData;
            // noinspection DataFlowIssue,UnstableApiUsage
            if (level.getModelDataManager() == null || (modelData = level.getModelDataManager().getAt(pos)) == null) {
                modelData = ModelData.EMPTY;
            }

            long randomSeed = blockState.getSeed(pos);
            RandomSource random = RandomSource.create();
            random.setSeed(randomSeed);

            for (RenderType renderType : model.getRenderTypes(blockState, random, modelData)) {
                VertexConsumer consumer = buffer.getBuffer(renderType);
                blockRenderDispatcher.getModelRenderer()
                        .tesselateBlock(level, model, blockState, pos,
                                poseStack, consumer, true, random, randomSeed,
                                OverlayTexture.NO_OVERLAY, modelData, renderType);
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        BlockState blockState = blockEntity.getBlockState();
        BakedModel model = blockRenderDispatcher.getBlockModel(blockState);

        if (model instanceof IBlockEntityRendererBakedModel<?> berModel) {
            if (berModel.getBlockEntityType() == blockEntity.getType()) {
                return ((IBlockEntityRendererBakedModel<T>) berModel).shouldRenderOffScreen(blockEntity);
            }
        }
        return BlockEntityRenderer.super.shouldRenderOffScreen(blockEntity);
    }

    @Override
    public boolean shouldRender(T blockEntity, Vec3 cameraPos) {
        BlockState blockState = blockEntity.getBlockState();
        BakedModel model = blockRenderDispatcher.getBlockModel(blockState);

        if (model instanceof IBlockEntityRendererBakedModel<?> berModel) {
            if (berModel.getBlockEntityType() == blockEntity.getType()) {
                return ((IBlockEntityRendererBakedModel<T>) berModel).shouldRender(blockEntity, cameraPos);
            }
        }
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos);
    }
}
