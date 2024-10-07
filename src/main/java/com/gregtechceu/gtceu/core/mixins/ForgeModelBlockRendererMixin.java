package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.client.model.GTMetadataSection;
import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.lighting.ForgeModelBlockRenderer;
import net.minecraftforge.client.model.lighting.QuadLighter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ForgeModelBlockRenderer.class, remap = false)
public class ForgeModelBlockRendererMixin {

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/model/lighting/QuadLighter;process(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;I)V"))
    private static void gtceu$renderToEmissiveBuffer(QuadLighter instance,
                                                     VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad, int overlay,
                                                     Operation<Void> original,
                                                     VertexConsumer _consumer, QuadLighter lighter, BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack) {
        if (GTShaders.allowedShader() && (!quad.isShade() || GTMetadataSection.hasBloom(quad.getSprite()))) {
            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            poseStack.scale(2f, 2f, 2f);
            original.call(instance, GTShaders.BLOOM_BUFFER_BUILDER, poseStack.last(), quad, overlay);
            poseStack.popPose();
        }
        original.call(instance, consumer, pose, quad, overlay);
    }
}
