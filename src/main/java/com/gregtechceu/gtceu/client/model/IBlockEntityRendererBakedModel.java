package com.gregtechceu.gtceu.client.model;

import com.gregtechceu.gtceu.client.model.compat.IDynamicBakedModel;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;

public interface IBlockEntityRendererBakedModel<T extends BlockEntity>
                                               extends IDynamicBakedModel,
                                               BlockEntityRenderer<T, BlockEntityRenderState> {

    @Nullable
    BlockEntityType<? extends T> getBlockEntityType();

    void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                int packedLight, int packedOverlay);

    default void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
                              PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {}

    default boolean shouldRender(T blockEntity, Vec3 cameraPos) {
        return Vec3.atCenterOf(blockEntity.getBlockPos()).closerThan(cameraPos, this.getViewDistance());
    }

    @Override
    default BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    @Override
    default void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                        CameraRenderState camera) {}

    default AABB getRenderBoundingBox(T blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return AABB.encapsulatingFullBlocks(pos.offset(-1, -1, -1), pos.offset(1, 1, 1));
    }

    @Override
    default boolean isCustomRenderer() {
        return true;
    }
}
