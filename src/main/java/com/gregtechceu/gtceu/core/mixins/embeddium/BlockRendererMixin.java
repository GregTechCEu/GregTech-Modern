package com.gregtechceu.gtceu.core.mixins.embeddium;

import com.gregtechceu.gtceu.client.bloom.BloomUtil;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.ArrayHelpers;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.sugar.Local;
import me.jellysquid.mods.sodium.client.model.color.ColorProvider;
import me.jellysquid.mods.sodium.client.model.light.LightPipeline;
import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.quad.BakedQuadView;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = BlockRenderer.class, remap = false)
public class BlockRendererMixin {

    @Inject(method = "renderQuadList",
            at = @At(value = "INVOKE",
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;writeGeometry(Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext;Lme/jellysquid/mods/sodium/client/render/chunk/compile/buffers/ChunkModelBuilder;Lnet/minecraft/world/phys/Vec3;Lme/jellysquid/mods/sodium/client/render/chunk/terrain/material/Material;Lme/jellysquid/mods/sodium/client/model/quad/BakedQuadView;[ILme/jellysquid/mods/sodium/client/model/light/data/QuadLightData;)V"))
    private void gtceu$captureBloomQuads(BlockRenderContext ctx, Material material, LightPipeline lighter,
                                         ColorProvider<BlockState> colorizer, Vec3 offset, ChunkModelBuilder builder,
                                         List<BakedQuad> quads, Direction cullFace,
                                         CallbackInfo ci,
                                         @Local(name = "quad") BakedQuadView quad,
                                         @Local(name = "lightData") QuadLightData lightData,
                                         @Local(name = "vertexColors") int[] vertexColors) {
        if (!ConfigHolder.INSTANCE.client.shader.emissiveTexturesHaveBloom || !GTShaders.canUseBloomShader()) {
            return;
        }
        // don't capture quads that are already on the bloom layer
        if (ctx.renderLayer() == GTRenderTypes.bloom()) return;

        float avgR = FastColor.ABGR32.red(vertexColors[0]);
        float avgG = FastColor.ABGR32.green(vertexColors[0]);
        float avgB = FastColor.ABGR32.blue(vertexColors[0]);
        if (!ArrayHelpers.allMatch(vertexColors)) {
            // only do the averaging if the vertex color isn't the same for all quads.
            // I think this should be a pretty good optimization?
            for (int i = 1; i < 4; i++) {
                // Sodium has per-vertex quad tint in ABGR. Average it
                avgR += FastColor.ABGR32.red(vertexColors[i]);
                avgG += FastColor.ABGR32.green(vertexColors[i]);
                avgB += FastColor.ABGR32.blue(vertexColors[i]);
            }
            avgR *= 0.25f; avgG *= 0.25f; avgB *= 0.25f;
        }

        BloomUtil.captureBloomQuad((BakedQuad) quad, ctx.renderLayer(), ctx.pos(), null,
                lightData.lm, OverlayTexture.NO_OVERLAY, lightData.br, avgR / 255f, avgG / 255f, avgB / 255f);
    }
}
