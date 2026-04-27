package com.gregtechceu.gtceu.core.mixins.client.bloom;

import com.gregtechceu.gtceu.client.bloom.BloomUtil;
import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.util.CapturedQuadData;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("SameReturnValue")
@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {

    @Unique
    private static final ThreadLocal<CapturedQuadData> gtceu$currentRenderType_tl = ThreadLocal
            .withInitial(CapturedQuadData::new);

    @WrapMethod(method = {
            "tesselateWithAO(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JILnet/minecraftforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V",
            "tesselateWithoutAO(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JILnet/minecraftforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V"
    }, remap = false)
    private void gtceu$captureBloomQuads$1(BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos,
                                           PoseStack poseStack, VertexConsumer consumer, boolean checkSides,
                                           RandomSource random, long seed, int packedOverlay,
                                           ModelData modelData, RenderType renderType,
                                           Operation<Void> original) {
        if (ConfigHolder.INSTANCE.client.shader.emissiveTexturesHaveBloom && GTShaders.canUseBloomShader()) {
            try (var $ = gtceu$currentRenderType_tl.get().with(renderType, pos)) {
                original.call(level, model, state, pos, poseStack, consumer, checkSides, random, seed, packedOverlay,
                        modelData, renderType);
            }
        } else {
            original.call(level, model, state, pos, poseStack, consumer, checkSides, random, seed, packedOverlay,
                    modelData, renderType);
        }
    }

    @WrapWithCondition(method = "putQuadData",
                       at = @At(value = "INVOKE",
                                target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFF[IIZ)V"))
    private boolean gtceu$captureBloomQuads$2(VertexConsumer instance, PoseStack.Pose poseEntry, BakedQuad quad,
                                              float[] brightness, float red, float green, float blue,
                                              int[] packedLights, int packedOverlay, boolean mulColor) {
        if (!ConfigHolder.INSTANCE.client.shader.emissiveTexturesHaveBloom || !GTShaders.canUseBloomShader()) {
            return true;
        }

        CapturedQuadData currentData = gtceu$currentRenderType_tl.get();
        BloomUtil.captureBloomQuad(quad, currentData.renderType(), currentData.pos(), poseEntry.pose(),
                packedLights, packedOverlay, brightness, red, green, blue);
        return true;
    }
}
