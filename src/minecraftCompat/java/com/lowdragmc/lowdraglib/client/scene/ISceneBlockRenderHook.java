package com.lowdragmc.lowdraglib.client.scene;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.blaze3d.vertex.PoseStack;

public interface ISceneBlockRenderHook {

    default void apply(boolean isTESR, RenderType renderType) {}

    default void applyBESR(Level level, BlockPos pos, BlockEntity blockEntity, PoseStack poseStack,
                           float partialTicks) {}

    default void applyVertexConsumerWrapper(Level level, BlockPos pos, BlockState state,
                                            WorldSceneRenderer.VertexConsumerWrapper wrapperBuffer,
                                            RenderType renderType, float partialTicks) {}
}
