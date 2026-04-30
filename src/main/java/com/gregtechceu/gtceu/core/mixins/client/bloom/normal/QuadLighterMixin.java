package com.gregtechceu.gtceu.core.mixins.client.bloom.normal;

import com.gregtechceu.gtceu.client.bloom.BloomUtil;
import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.gregtechceu.gtceu.core.IGTQuadLighter;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraftforge.client.model.lighting.QuadLighter;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("SameReturnValue")
@Mixin(value = QuadLighter.class, remap = false)
public class QuadLighterMixin implements IGTQuadLighter {

    @Shadow
    private BlockPos pos;
    @Unique
    private @Nullable RenderType gtceu$renderType;

    @WrapWithCondition(method = "process",
                       at = @At(value = "INVOKE",
                                target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFF[IIZ)V"))
    private boolean gtceu$skipBloomyQuadsFromModel(VertexConsumer instance, PoseStack.Pose poseEntry,
                                                   BakedQuad quad, float[] brightness,
                                                   float red, float green, float blue,
                                                   int[] packedLights, int packedOverlay,
                                                   boolean mulColor) {
        if (!GTShaders.isBloomShaderInUse()) return true;

        BloomUtil.captureBloomQuad(quad, this.gtceu$renderType, this.pos, poseEntry.pose(),
                packedLights, packedOverlay, brightness, red, green, blue);
        return true;
    }

    @Override
    public void gtceu$setRenderType(RenderType currentRenderType) {
        this.gtceu$renderType = currentRenderType;
    }
}
