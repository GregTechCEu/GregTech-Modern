package com.gregtechceu.gtceu.core.mixins.client.bloom.normal.embeddium;

import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.gregtechceu.gtceu.client.util.TextureMetadataHelper;
import com.gregtechceu.gtceu.integration.embeddium.GTEmbeddiumCompat;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.quad.BakedQuadView;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import org.embeddedt.embeddium.impl.render.chunk.compile.GlobalChunkBuildContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BlockRenderer.class, remap = false)
public class BlockRendererMixin {

    @WrapOperation(method = "renderQuadList",
                   at = @At(value = "INVOKE",
                            target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;writeGeometry(Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext;Lme/jellysquid/mods/sodium/client/render/chunk/compile/buffers/ChunkModelBuilder;Lnet/minecraft/world/phys/Vec3;Lme/jellysquid/mods/sodium/client/render/chunk/terrain/material/Material;Lme/jellysquid/mods/sodium/client/model/quad/BakedQuadView;[ILme/jellysquid/mods/sodium/client/model/light/data/QuadLightData;)V"))
    private void gtceu$copyBloomQuads(BlockRenderer instance, BlockRenderContext ctx, ChunkModelBuilder originalBuilder,
                                      Vec3 offset, Material material, BakedQuadView quad,
                                      int[] vertexColors, QuadLightData lightData,
                                      Operation<Void> original,
                                      @Share("bloomBuilder") LocalRef<ChunkModelBuilder> bloomBuilderRef) {
        original.call(instance, ctx, originalBuilder, offset, material, quad, vertexColors, lightData);

        if (!GTShaders.isBloomShaderInUse()) return;

        ChunkBuildContext chunkContext = GlobalChunkBuildContext.get();
        if (chunkContext != null && TextureMetadataHelper.hasBloom((BakedQuad) quad, lightData.lm)) {
            var bloomBuilder = chunkContext.buffers.get(GTEmbeddiumCompat.BLOOM_RENDER_PASS);
            bloomBuilderRef.set(bloomBuilder);

            // call the same method again, this time with the bloom chunk model builder
            original.call(instance, ctx, bloomBuilder, offset, material, quad, vertexColors, lightData);
        } else {
            bloomBuilderRef.set(null);
        }
    }

    @WrapOperation(method = "renderQuadList",
                   at = @At(value = "INVOKE",
                            target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/buffers/ChunkModelBuilder;addSprite(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"))
    private void gtceu$copyBloomSpriteAdds(ChunkModelBuilder originalBuilder, TextureAtlasSprite sprite,
                                           Operation<Void> original,
                                           @Share("bloomBuilder") LocalRef<ChunkModelBuilder> bloomBuilderRef) {
        original.call(originalBuilder, sprite);

        // set by the above inject; value is only non-null when all appropriate conditions/requirements apply.
        // thus no need to check them here.
        var bloomBuilder = bloomBuilderRef.get();
        if (bloomBuilder != null) {
            original.call(bloomBuilder, sprite);
        }
    }
}
