package com.gregtechceu.gtceu.client.bloom;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;

/**
 * Render callback interface for {@link BloomUtil#registerBloomRender(IRenderSetup, IBloomEffect, BlockEntity)}.
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
    void renderBloomEffect(@NotNull PoseStack poseStack, @NotNull BufferBuilder buffer,
                           @NotNull EffectRenderContext context);

    /**
     * @param context render context
     * @return if this effect should be rendered; returning {@code false} skips calling
     *         {@link #renderBloomEffect(PoseStack, BufferBuilder, EffectRenderContext)}.
     */
    @OnlyIn(Dist.CLIENT)
    default boolean shouldRenderBloomEffect(@NotNull EffectRenderContext context) {
        return true;
    }
}
