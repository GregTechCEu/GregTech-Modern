package com.gregtechceu.gtceu.core.mixins.embeddium;

import com.gregtechceu.gtceu.client.model.BloomMetadataSection;
import com.gregtechceu.gtceu.client.shader.GTShaders;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.quad.BakedQuadView;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BlockRenderer.class, remap = false)
public class BlockRendererMixin {

    @WrapWithCondition(method = "renderQuadList",
                   at = @At(value = "INVOKE",
                            target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;writeGeometry(Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext;Lme/jellysquid/mods/sodium/client/render/chunk/compile/buffers/ChunkModelBuilder;Lnet/minecraft/world/phys/Vec3;Lme/jellysquid/mods/sodium/client/render/chunk/terrain/material/Material;Lme/jellysquid/mods/sodium/client/model/quad/BakedQuadView;[ILme/jellysquid/mods/sodium/client/model/light/data/QuadLightData;)V"))
    private boolean gtceu$captureBloomQuads(BlockRenderer instance, BlockRenderContext ctx, ChunkModelBuilder builder,
                                            Vec3 offset, Material material, BakedQuadView quad,
                                            int[] colors, QuadLightData light) {
        if (!GTShaders.canUseBloomShader()) return true;

        return BloomMetadataSection.shouldDrawQuad((BakedQuad) quad, ctx.renderLayer(), light.lm);
    }
}
