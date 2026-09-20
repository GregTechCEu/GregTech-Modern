package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.client.model.IBlockEntityRendererBakedModel;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;

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
            if (berModel.getBlockEntityType() != null && berModel.getBlockEntityType() != blockEntity.getType()) return;

            ((IBlockEntityRendererBakedModel<T>) berModel).render(blockEntity, partialTick,
                    poseStack, buffer, packedLight, packedOverlay);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        BlockState blockState = blockEntity.getBlockState();
        BakedModel model = blockRenderDispatcher.getBlockModel(blockState);

        if (model instanceof IBlockEntityRendererBakedModel<?> berModel) {
            if (berModel.getBlockEntityType() != null && berModel.getBlockEntityType() == blockEntity.getType()) {
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
            if (berModel.getBlockEntityType() != null && berModel.getBlockEntityType() == blockEntity.getType()) {
                return ((IBlockEntityRendererBakedModel<T>) berModel).shouldRender(blockEntity, cameraPos);
            }
        }
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos);
    }
}
