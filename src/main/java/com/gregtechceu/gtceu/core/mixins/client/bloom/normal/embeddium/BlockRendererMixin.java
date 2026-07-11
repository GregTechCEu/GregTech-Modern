package com.gregtechceu.gtceu.core.mixins.client.bloom.normal.embeddium;

import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;
import com.gregtechceu.gtceu.client.util.TextureMetadataHelper;
import com.gregtechceu.gtceu.integration.embeddium.GTEmbeddiumCompat;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.embeddedt.embeddium.api.render.chunk.BlockRenderContext;
import org.embeddedt.embeddium.impl.model.color.ColorProvider;
import org.embeddedt.embeddium.impl.model.light.LightPipeline;
import org.embeddedt.embeddium.impl.model.light.data.QuadLightData;
import org.embeddedt.embeddium.impl.model.quad.BakedQuadView;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildContext;
import org.embeddedt.embeddium.impl.render.chunk.compile.GlobalChunkBuildContext;
import org.embeddedt.embeddium.impl.render.chunk.compile.buffers.ChunkModelBuilder;
import org.embeddedt.embeddium.impl.render.chunk.compile.pipeline.BlockRenderer;
import org.embeddedt.embeddium.impl.render.chunk.terrain.material.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = BlockRenderer.class, remap = false)
public abstract class BlockRendererMixin {

    @Shadow
    protected abstract void writeGeometry(BlockRenderContext ctx, ChunkModelBuilder builder, Vec3 offset,
                                          Material material, BakedQuadView quad, int[] colors, QuadLightData light);

    @Inject(method = "renderQuadList",
            at = @At(value = "INVOKE",
                     target = "Lorg/embeddedt/embeddium/impl/render/chunk/compile/pipeline/BlockRenderer;writeGeometry(Lorg/embeddedt/embeddium/api/render/chunk/BlockRenderContext;Lorg/embeddedt/embeddium/impl/render/chunk/compile/buffers/ChunkModelBuilder;Lnet/minecraft/world/phys/Vec3;Lorg/embeddedt/embeddium/impl/render/chunk/terrain/material/Material;Lorg/embeddedt/embeddium/impl/model/quad/BakedQuadView;[ILorg/embeddedt/embeddium/impl/model/light/data/QuadLightData;)V",
                     shift = At.Shift.AFTER))
    private void gtceu$copyBloomQuads(BlockRenderContext ctx, Material material, LightPipeline lighter,
                                      ColorProvider<BlockState> colorizer, Vec3 offset, ChunkModelBuilder builder,
                                      List<BakedQuad> quads, Direction cullFace, CallbackInfo ci,
                                      @Local(name = "quad") BakedQuadView quad,
                                      @Local(name = "vertexColors") int[] vertexColors,
                                      @Local(name = "lightData") QuadLightData lightData,
                                      @Share("bloomBuilder") LocalRef<ChunkModelBuilder> bloomBuilderRef) {
        ChunkBuildContext chunkContext = GlobalChunkBuildContext.get();
        if (BloomShaderManager.isBloomActive() && chunkContext != null &&
                TextureMetadataHelper.hasBloom((BakedQuad) quad, lightData.lm)) {
            var bloomBuilder = chunkContext.buffers.get(GTEmbeddiumCompat.BLOOM_RENDER_PASS);
            bloomBuilderRef.set(bloomBuilder);

            // call the same method again, this time with the bloom chunk model builder
            this.writeGeometry(ctx, bloomBuilder, offset, GTEmbeddiumCompat.BLOOM_MATERIAL, quad, vertexColors,
                    lightData);
        } else {
            bloomBuilderRef.set(null);
        }
    }

    @Inject(method = "renderQuadList",
            at = @At(value = "INVOKE",
                     target = "Lorg/embeddedt/embeddium/impl/render/chunk/compile/buffers/ChunkModelBuilder;addSprite(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V",
                     shift = At.Shift.AFTER))
    private void gtceu$copyBloomSpriteAdds(BlockRenderContext ctx, Material material, LightPipeline lighter,
                                           ColorProvider<BlockState> colorizer, Vec3 offset, ChunkModelBuilder builder,
                                           List<BakedQuad> quads, Direction cullFace,
                                           CallbackInfo ci,
                                           @Local(name = "sprite") TextureAtlasSprite sprite,
                                           @Share("bloomBuilder") LocalRef<ChunkModelBuilder> bloomBuilderRef) {
        // set by the above inject; value is only non-null when all appropriate conditions/requirements apply.
        // thus no need to check them here.
        var bloomBuilder = bloomBuilderRef.get();
        if (bloomBuilder != null) {
            bloomBuilder.addSprite(sprite);
        }
    }
}
