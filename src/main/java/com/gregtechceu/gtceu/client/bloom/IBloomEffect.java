package com.gregtechceu.gtceu.client.bloom;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Render callback interface for {@link BloomHandler#registerBloomRender(IRenderSetup, IBloomEffect, BlockEntity)}.
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
    void renderBloomEffect(PoseStack poseStack, BufferBuilder buffer, EffectRenderContext context);

    /**
     * @param context render context
     * @return if this effect should be rendered; returning {@code false} skips calling
     *         {@link #renderBloomEffect(PoseStack, BufferBuilder, EffectRenderContext)}.
     */
    @OnlyIn(Dist.CLIENT)
    default boolean shouldRenderBloomEffect(EffectRenderContext context) {
        return true;
    }
}
