package com.gregtechceu.gtceu.core.mixins.client.bloom;

import com.gregtechceu.gtceu.client.model.BloomMetadataSection;
import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.gregtechceu.gtceu.core.util.ContextualObjectHelper;

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

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {

    @Unique
    private static final ThreadLocal<ContextualObjectHelper<RenderType>> gtceu$currentRenderType_thr = ThreadLocal
            .withInitial(ContextualObjectHelper::new);

    @WrapMethod(method = {
            "tesselateWithAO(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JILnet/minecraftforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V",
            "tesselateWithoutAO(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JILnet/minecraftforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V"
    }, remap = false)
    private static void gtceu$removeBloomyQuadsFromModel$1$1(BlockAndTintGetter level, BakedModel model,
                                                             BlockState state, BlockPos pos, PoseStack poseStack,
                                                             VertexConsumer consumer, boolean checkSides,
                                                             RandomSource random, long seed, int packedOverlay,
                                                             ModelData modelData, RenderType renderType,
                                                             Operation<Void> original) {
        if (!GTShaders.canUseBloomShader()) return;

        try (var $ = gtceu$currentRenderType_thr.get().with(renderType)) {
            original.call(level, model, state, pos, poseStack, consumer, checkSides, random, seed, packedOverlay,
                    modelData, renderType);
        }
    }

    @WrapMethod(method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/resources/model/BakedModel;FFFIILnet/minecraftforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V",
            remap = false)
    private static void gtceu$removeBloomyQuadsFromModel$2$1(PoseStack.Pose pose, VertexConsumer consumer,
                                                             BlockState state, BakedModel model,
                                                             float red, float green, float blue,
                                                             int packedLight, int packedOverlay,
                                                             ModelData modelData, RenderType renderType,
                                                             Operation<Void> original) {
        if (!GTShaders.canUseBloomShader()) return;

        try (var $ = gtceu$currentRenderType_thr.get().with(renderType)) {
            original.call(pose, consumer, state, model, red, green, blue, packedLight, packedOverlay,
                    modelData, renderType);
        }
    }

    @WrapWithCondition(method = "putQuadData",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFF[IIZ)V")
    )
    private static boolean gtceu$skipBloomyQuadsFromModel$1$2(VertexConsumer instance,
                                                              PoseStack.Pose poseEntry, BakedQuad quad,
                                                              float[] colorMuls, float red, float green, float blue,
                                                              int[] combinedLights, int combinedOverlay,
                                                              boolean mulColor) {
        if (!GTShaders.canUseBloomShader()) return true;

        RenderType currentRenderType = gtceu$currentRenderType_thr.get().getCurrent();

        return BloomMetadataSection.shouldDrawQuad(quad, currentRenderType, combinedLights);
    }

    @WrapWithCondition(method = "renderQuadList",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;FFFII)V")
    )
    private static boolean gtceu$skipBloomyQuadsFromModel$2$2(VertexConsumer instance,
                                                              PoseStack.Pose poseEntry, BakedQuad quad,
                                                              float red, float green, float blue,
                                                              int combinedLight, int combinedOverlay) {
        if (!GTShaders.canUseBloomShader()) return true;

        RenderType currentRenderType = gtceu$currentRenderType_thr.get().getCurrent();
        int[] combinedLights = new int[] { combinedLight, combinedLight, combinedLight, combinedLight };

        return BloomMetadataSection.shouldDrawQuad(quad, currentRenderType, combinedLights);
    }
}
