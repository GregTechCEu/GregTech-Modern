package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.renderer.IRenderSetup;
import com.gregtechceu.gtceu.client.shader.post.BloomType;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Render callback interface for
 * {@link BloomEffectUtil#registerBloomRender(IRenderSetup, BloomType, IBloomEffect, BlockEntity)}.
 */
@FunctionalInterface
public interface IBloomEffect {

    /**
     * Render the bloom effect.
     *
     * @param buffer  buffer builder
     * @param context render context
     */
    @OnlyIn(Dist.CLIENT)
    void renderBloomEffect(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, @NotNull EffectRenderContext context);

    /**
     * @param context render context
     * @return if this effect should be rendered; returning {@code false} skips
     *         {@link #renderBloomEffect(PoseStack, MultiBufferSource, EffectRenderContext)} call.
     */
    @OnlyIn(Dist.CLIENT)
    default boolean shouldRenderBloomEffect(@NotNull EffectRenderContext context) {
        return true;
    }
}
