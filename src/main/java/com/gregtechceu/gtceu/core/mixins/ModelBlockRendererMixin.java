package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.client.model.GTMetadataSection;
import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {

    @WrapOperation(method = "renderModelFaceAO",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;putQuadData(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;FFFFIIIII)V"))
    private void gtceu$renderToEmissiveBuffer(ModelBlockRenderer instance,
                                              BlockAndTintGetter level, BlockState state, BlockPos pos,
                                              VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad,
                                              float brightness0, float brightness1, float brightness2, float brightness3,
                                              int lightmap0, int lightmap1, int lightmap2, int lightmap3,
                                              int packedOverlay,
                                              Operation<Void> original,
                                              BlockAndTintGetter _level, BlockState _state, BlockPos _pos, PoseStack poseStack) {
        if (GTShaders.allowedShader() && (!quad.isShade() || GTMetadataSection.hasBloom(quad.getSprite()))) {
            poseStack.pushPose();
            poseStack.translate(pos.getX() - (pos.getX() & 15), pos.getY() - (pos.getY() & 15), pos.getZ() - (pos.getZ() & 15));
            //poseStack.scale(2f, 2f, 2f);
            original.call(instance, level, state, pos,
                    GTShaders.BLOOM_BUFFER_BUILDER, poseStack.last(), quad,
                    brightness0, brightness1, brightness2, brightness3,
                    lightmap0, lightmap1, lightmap2, lightmap3,
                    packedOverlay);
            poseStack.popPose();
        }
        original.call(instance, level, state, pos,
                consumer, pose, quad,
                brightness0, brightness1, brightness2, brightness3,
                lightmap0, lightmap1, lightmap2, lightmap3,
                packedOverlay);
    }

    @WrapOperation(method = "renderModelFaceFlat",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;putQuadData(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;FFFFIIIII)V"))
    private void gtceu$renderToEmissiveBuffer(ModelBlockRenderer instance,
                                              BlockAndTintGetter level, BlockState state, BlockPos pos,
                                              VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad,
                                              float brightness0, float brightness1, float brightness2, float brightness3,
                                              int lightmap0, int lightmap1, int lightmap2, int lightmap3,
                                              int packedOverlay,
                                              Operation<Void> original,
                                              BlockAndTintGetter _level, BlockState _state, BlockPos _pos, int packedLight, int _packedOverlay, boolean repackLight, PoseStack poseStack) {
        if (GTShaders.allowedShader() && (!quad.isShade() || GTMetadataSection.hasBloom(quad.getSprite()))) {
            poseStack.pushPose();
            poseStack.translate(pos.getX() - (pos.getX() & 15), pos.getY() - (pos.getY() & 15), pos.getZ() - (pos.getZ() & 15));
            //poseStack.scale(2f, 2f, 2f);
            original.call(instance, level, state, pos,
                    GTShaders.BLOOM_BUFFER_BUILDER, poseStack.last(), quad,
                    brightness0, brightness1, brightness2, brightness3,
                    lightmap0, lightmap1, lightmap2, lightmap3,
                    packedOverlay);
            poseStack.popPose();
        }
        original.call(instance, level, state, pos,
                consumer, pose, quad,
                brightness0, brightness1, brightness2, brightness3,
                lightmap0, lightmap1, lightmap2, lightmap3,
                packedOverlay);
    }
}
