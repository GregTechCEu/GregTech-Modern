package com.gregtechceu.gtceu.core.mixins.client.bloom;

import com.gregtechceu.gtceu.client.bloom.BloomRenderer;
import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;
import com.gregtechceu.gtceu.utils.ScopedValue;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {

    @Unique
    private static final ThreadLocal<ScopedValue.Object<RenderType>> gtceu$currentRenderType = ThreadLocal
            .withInitial(ScopedValue.Object::new);

    @WrapMethod(method = {
            "tesselateWithAO(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JILnet/neoforged/neoforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V",
            "tesselateWithoutAO(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JILnet/neoforged/neoforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V"
    }, remap = false)
    private void gtceu$copyBloomQuads$1(BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos,
                                        PoseStack poseStack, VertexConsumer consumer, boolean checkSides,
                                        RandomSource random, long seed, int packedOverlay,
                                        ModelData modelData, RenderType renderType,
                                        Operation<Void> original) {
        try (var $ = gtceu$currentRenderType.get().with(renderType)) {
            original.call(level, model, state, pos, poseStack, consumer, checkSides, random, seed, packedOverlay,
                    modelData, renderType);
        }
    }

    // The arguments don't have locals, so there's no good way to capture them except a @WarpWith(Condition) injector
    @WrapOperation(method = "putQuadData",
                   at = @At(value = "INVOKE",
                            target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFFF[IIZ)V"))
    private void gtceu$copyBloomQuads(VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad,
                                      float[] brightness, float red, float green, float blue, float alpha,
                                      int[] lightmap, int overlay, boolean readAlpha,
                                      Operation<Void> original) {
        original.call(consumer, pose, quad, brightness, red, green, blue, alpha, lightmap, overlay, readAlpha);

        if (!BloomShaderManager.isBloomActive()) return;

        RenderType renderType = gtceu$currentRenderType.get().getValue();
        BloomRenderer.copyBloomQuad(quad, lightmap, renderType, bloomVertexConsumer -> {
            original.call(bloomVertexConsumer, pose, quad, brightness, red, green, blue, alpha, lightmap, overlay,
                    readAlpha);
        });
    }
}
