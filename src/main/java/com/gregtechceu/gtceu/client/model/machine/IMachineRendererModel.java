package com.gregtechceu.gtceu.client.model.machine;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;
import com.gregtechceu.gtceu.client.model.IBlockEntityRendererBakedModel;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public interface IMachineRendererModel<T extends IMachineFeature> extends IBlockEntityRendererBakedModel<BlockEntity> {

    MachineDefinition getDefinition();

    void render(T machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                int packedLight, int packedOverlay);

    default void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
                              PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {}

    default boolean shouldRenderOffScreen(T machine) {
        return false;
    }

    default boolean shouldRender(T machine, Vec3 cameraPos) {
        return Vec3.atCenterOf(machine.self().getPos()).closerThan(cameraPos, this.getViewDistance());
    }

    default AABB getRenderBoundingBox(T machine) {
        BlockPos pos = machine.self().getPos();
        return new AABB(pos.offset(-1, 0, -1), pos.offset(2, 2, 2));
    }

    // region default impls

    @Override
    default BlockEntityType<? extends BlockEntity> getBlockEntityType() {
        return getDefinition().getBlockEntityType();
    }

    @ApiStatus.NonExtendable
    @SuppressWarnings("unchecked")
    @Override
    default void render(@NotNull BlockEntity blockEntity, float partialTick,
                        @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer,
                        int packedLight, int packedOverlay) {
        if (!(blockEntity instanceof IMachineBlockEntity machineBE)) return;
        if (machineBE.getDefinition() != getDefinition()) return;

        this.render((T) machineBE.getMetaMachine(), partialTick,
                poseStack, buffer, packedLight, packedOverlay);
    }

    @ApiStatus.NonExtendable
    @SuppressWarnings("unchecked")
    @Override
    default boolean shouldRenderOffScreen(@NotNull BlockEntity blockEntity) {
        if (!(blockEntity instanceof IMachineBlockEntity machineBE))
            return IBlockEntityRendererBakedModel.super.shouldRenderOffScreen(blockEntity);
        if (machineBE.getDefinition() != getDefinition())
            return IBlockEntityRendererBakedModel.super.shouldRenderOffScreen(blockEntity);

        return this.shouldRenderOffScreen((T) machineBE.getMetaMachine());
    }

    @ApiStatus.NonExtendable
    @SuppressWarnings("unchecked")
    @Override
    default boolean shouldRender(BlockEntity blockEntity, @NotNull Vec3 cameraPos) {
        if (!(blockEntity instanceof IMachineBlockEntity machineBE))
            return IBlockEntityRendererBakedModel.super.shouldRender(blockEntity, cameraPos);
        if (machineBE.getDefinition() != getDefinition())
            return IBlockEntityRendererBakedModel.super.shouldRender(blockEntity, cameraPos);

        return this.shouldRender((T) machineBE.getMetaMachine(), cameraPos);
    }

    @ApiStatus.NonExtendable
    @SuppressWarnings("unchecked")
    @Override
    default AABB getRenderBoundingBox(BlockEntity blockEntity) {
        if (!(blockEntity instanceof IMachineBlockEntity machineBE))
            return IBlockEntityRendererBakedModel.super.getRenderBoundingBox(blockEntity);
        if (machineBE.getDefinition() != getDefinition())
            return IBlockEntityRendererBakedModel.super.getRenderBoundingBox(blockEntity);

        return this.getRenderBoundingBox((T) machineBE.getMetaMachine());
    }

    // endregion
}
