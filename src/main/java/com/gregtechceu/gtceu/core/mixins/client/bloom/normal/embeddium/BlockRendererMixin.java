package com.gregtechceu.gtceu.core.mixins.client.bloom.normal.embeddium;

import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.util.TextureMetadataHelper;
import com.gregtechceu.gtceu.integration.embeddium.GTEmbeddiumCompat;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.sugar.Local;
import org.embeddedt.embeddium.api.render.chunk.BlockRenderContext;
import org.embeddedt.embeddium.impl.model.light.data.QuadLightData;
import org.embeddedt.embeddium.impl.model.quad.BakedQuadView;
import org.embeddedt.embeddium.impl.model.quad.properties.ModelQuadFacing;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildContext;
import org.embeddedt.embeddium.impl.render.chunk.compile.GlobalChunkBuildContext;
import org.embeddedt.embeddium.impl.render.chunk.compile.buffers.ChunkModelBuilder;
import org.embeddedt.embeddium.impl.render.chunk.compile.pipeline.BlockRenderer;
import org.embeddedt.embeddium.impl.render.chunk.terrain.material.Material;
import org.embeddedt.embeddium.impl.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexEncoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockRenderer.class, remap = false)
public abstract class BlockRendererMixin {

    @Inject(method = "writeGeometry",
            at = @At(value = "INVOKE",
                     target = "Lorg/embeddedt/embeddium/impl/render/chunk/vertex/builder/ChunkMeshBufferBuilder;push([Lorg/embeddedt/embeddium/impl/render/chunk/vertex/format/ChunkVertexEncoder$Vertex;Lorg/embeddedt/embeddium/impl/render/chunk/terrain/material/Material;)V",
                     shift = At.Shift.AFTER))
    private void gtceu$copyBloomQuads(BlockRenderContext ctx, ChunkModelBuilder builder, Vec3 offset, Material material,
                                      BakedQuadView quad, int[] colors, QuadLightData light,
                                      CallbackInfo ci,
                                      @Local(name = "vertices") ChunkVertexEncoder.Vertex[] vertices,
                                      @Local(name = "normalFace") ModelQuadFacing normalFace) {
        if (!BloomShaderManager.isBloomActive() || ctx.renderLayer() == GTRenderTypes.bloom()) {
            return;
        }
        ChunkBuildContext chunkContext = GlobalChunkBuildContext.get();
        if (chunkContext == null) {
            return;
        }

        if (TextureMetadataHelper.hasBloom((BakedQuad) quad, light.lm)) {
            var bloomBuilder = chunkContext.buffers.get(GTEmbeddiumCompat.getBloomRenderPass());

            // call the same method again, this time with the bloom chunk model builder
            ChunkMeshBufferBuilder vertexBuffer = bloomBuilder.getVertexBuffer(normalFace);
            vertexBuffer.push(vertices, GTEmbeddiumCompat.getBloomMaterial());

            TextureAtlasSprite atlasSprite = quad.getSprite();
            if (atlasSprite != null) {
                bloomBuilder.addSprite(atlasSprite);
            }
        }
    }
}
