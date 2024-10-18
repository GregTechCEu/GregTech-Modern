package com.gregtechceu.gtceu.core.mixins.embeddium;

import com.gregtechceu.gtceu.client.model.GTMetadataSection;
import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.gregtechceu.gtceu.client.util.BloomEffectUtil;

import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.quad.BakedQuadView;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadOrientation;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import me.jellysquid.mods.sodium.client.util.ModelQuadUtil;
import org.embeddedt.embeddium.render.chunk.ChunkColorWriter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BlockRenderer.class, remap = false)
public class BlockRendererMixin {

    @Shadow
    private boolean useReorienting;

    @Shadow
    @Final
    private ChunkVertexEncoder.Vertex[] vertices;

    @Shadow
    @Final
    private ChunkColorWriter colorEncoder;

    @WrapOperation(method = "renderQuadList",
                   at = @At(value = "INVOKE",
                            target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;writeGeometry(Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext;Lme/jellysquid/mods/sodium/client/render/chunk/compile/buffers/ChunkModelBuilder;Lnet/minecraft/world/phys/Vec3;Lme/jellysquid/mods/sodium/client/render/chunk/terrain/material/Material;Lme/jellysquid/mods/sodium/client/model/quad/BakedQuadView;[ILme/jellysquid/mods/sodium/client/model/light/data/QuadLightData;)V"))
    private void gtceu$captureBloomQuads(BlockRenderer instance, BlockRenderContext ctx, ChunkModelBuilder builder,
                                         Vec3 offset, Material material, BakedQuadView quad,
                                         int[] colors, QuadLightData light, Operation<Void> original) {
        BlockPos chunkOrigin = SectionPos.of(ctx.pos()).origin();
        // Check if quad is full brightness OR we have bloom enabled for the quad
        // TODO improve, don't mixin to embeddium, maybe ask for an API? doubt we'll get it though
        if (GTShaders.allowedShader() && (!quad.hasShade() || GTMetadataSection.hasBloom(quad.getSprite()))) {
            ModelQuadOrientation orientation = this.useReorienting ?
                    ModelQuadOrientation.orientByBrightness(light.br, light.lm) : ModelQuadOrientation.NORMAL;
            for (int dstIndex = 0; dstIndex < 4; ++dstIndex) {
                int srcIndex = orientation.getVertexIndex(dstIndex);
                int color = this.colorEncoder.writeColor(
                        ModelQuadUtil.mixARGBColors(colors[srcIndex], quad.getColor(srcIndex)), light.br[srcIndex]);
                float u = quad.getTexU(srcIndex);
                float v = quad.getTexV(srcIndex);
                int lightUv = ModelQuadUtil.mergeBakedLight(quad.getLight(srcIndex), light.lm[srcIndex]);
                int normal = quad.getForgeNormal(dstIndex);

                BloomEffectUtil.getOrStartBloomBuffer(chunkOrigin)
                        .vertex(ctx.origin().x() + quad.getX(srcIndex) + (float) offset.x(),
                                ctx.origin().y() + quad.getY(srcIndex) + (float) offset.y(),
                                ctx.origin().z() + quad.getZ(srcIndex) + (float) offset.z(),
                                ColorARGB.unpackRed(color) * 255.0f,
                                ColorARGB.unpackGreen(color) * 255.0f,
                                ColorARGB.unpackBlue(color) * 255.0f,
                                ColorARGB.unpackAlpha(color) * 255.0f,
                                u, v,
                                OverlayTexture.NO_OVERLAY,
                                lightUv,
                                NormI8.unpackX(normal),
                                NormI8.unpackY(normal),
                                NormI8.unpackZ(normal));
            }
        }
        original.call(instance, ctx, builder, offset, material, quad, colors, light);
    }
}
