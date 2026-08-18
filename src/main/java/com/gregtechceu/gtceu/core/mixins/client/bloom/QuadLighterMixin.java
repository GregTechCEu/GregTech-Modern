package com.gregtechceu.gtceu.core.mixins.client.bloom;

import com.gregtechceu.gtceu.client.bloom.BloomRenderer;
import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;
import com.gregtechceu.gtceu.core.util.extensions.QuadLighterExt;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.client.model.lighting.QuadLighter;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("SameReturnValue")
@Mixin(value = QuadLighter.class, remap = false)
public class QuadLighterMixin implements QuadLighterExt {

    @Unique
    private @Nullable RenderType gtceu$renderType;

    @WrapOperation(method = "process",
                   at = @At(value = "INVOKE",
                            target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFF[IIZ)V",
                            remap = true))
    private void gtceu$copyBloomQuads(VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad,
                                      float[] colorMuls, float red, float green, float blue,
                                      int[] combinedLights, int combinedOverlay, boolean mulColor,
                                      Operation<Void> original) {
        original.call(consumer, pose, quad, colorMuls, red, green, blue, combinedLights, combinedOverlay, mulColor);

        if (this.gtceu$renderType == null) return;
        if (!BloomShaderManager.isBloomActive()) return;

        BloomRenderer.copyBloomQuad(quad, combinedLights, this.gtceu$renderType, bloomVertexConsumer -> {
            original.call(bloomVertexConsumer, pose, quad, colorMuls, red, green, blue,
                    combinedLights, combinedOverlay, mulColor);
        });
    }

    @Override
    public void gtceu$setRenderType(RenderType currentRenderType) {
        this.gtceu$renderType = currentRenderType;
    }
}
