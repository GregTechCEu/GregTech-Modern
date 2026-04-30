package com.gregtechceu.gtceu.core.mixins.client.bloom.normal;

import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.IGTQuadLighter;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.lighting.ForgeModelBlockRenderer;
import net.minecraftforge.client.model.lighting.QuadLighter;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ForgeModelBlockRenderer.class, remap = false)
public class ForgeModelBlockRendererMixin {

    @Inject(method = "render",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraftforge/client/model/lighting/QuadLighter;setup(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private static void gtceu$setQuadLighterRenderType(VertexConsumer vertexConsumer, QuadLighter lighter,
                                                       BlockAndTintGetter level, BakedModel model, BlockState state,
                                                       BlockPos pos, PoseStack poseStack, boolean checkSides,
                                                       RandomSource rand, long seed, int packedOverlay,
                                                       ModelData modelData, RenderType renderType,
                                                       CallbackInfoReturnable<Boolean> cir,
                                                       @Local(name = "flatLighter") QuadLighter flatLighter) {
        if (ConfigHolder.INSTANCE.client.bloom.safeMode) return;
        if (!GTShaders.canUseBloomShader()) return;

        if (flatLighter != null) {
            // this is always in the flatLighter init block
            ((IGTQuadLighter) flatLighter).gtceu$setRenderType(renderType);
        } else {
            // and this _should_ always be outside of it
            ((IGTQuadLighter) lighter).gtceu$setRenderType(renderType);
        }
    }
}
