package com.gregtechceu.gtceu.client.model;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.IDynamicBakedModel;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;

public interface IBlockEntityRendererBakedModel<T extends BlockEntity>
                                               extends IDynamicBakedModel, BlockEntityRenderer<T> {

    BlockEntityType<? extends T> getBlockEntityType();

    void render(@NotNull T blockEntity, float partialTick,
                @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer,
                int packedLight, int packedOverlay);

    default void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
                              PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {}

    default boolean shouldRender(T blockEntity, @NotNull Vec3 cameraPos) {
        return Vec3.atCenterOf(blockEntity.getBlockPos()).closerThan(cameraPos, this.getViewDistance());
    }

    default AABB getRenderBoundingBox(T blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.offset(-1, 0, -1), pos.offset(2, 2, 2));
    }

    @Override
    default boolean isCustomRenderer() {
        return true;
    }
}
