package com.gregtechceu.gtceu.core.mixins.client.customchunk.embeddium;

import com.gregtechceu.gtceu.client.bloom.BloomRenderer;
import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;
import com.gregtechceu.gtceu.client.renderer.FaceLayerRouting;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.util.TextureMetadataHelper;
import com.gregtechceu.gtceu.integration.embeddium.GTEmbeddiumCompat;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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

@Mixin(value = BlockRenderer.class, remap = false)
public abstract class EmbeddiumBlockRendererMixin {

    @WrapOperation(method = "writeGeometry",
                   at = @At(value = "INVOKE",
                            target = "Lorg/embeddedt/embeddium/impl/render/chunk/vertex/builder/ChunkMeshBufferBuilder;push([Lorg/embeddedt/embeddium/impl/render/chunk/vertex/format/ChunkVertexEncoder$Vertex;Lorg/embeddedt/embeddium/impl/render/chunk/terrain/material/Material;)V"))
    private void gtceu$routeCustomPassQuads(ChunkMeshBufferBuilder originalBuilder,
                                            ChunkVertexEncoder.Vertex[] vertices, Material originalMaterial,
                                            Operation<Void> original,
                                            BlockRenderContext ctx, ChunkModelBuilder builder, Vec3 offset,
                                            Material material, BakedQuadView quad, int[] colors,
                                            QuadLightData light,
                                            @Local(name = "normalFace") ModelQuadFacing normalFace) {
        ChunkBuildContext chunkContext = GlobalChunkBuildContext.get();
        if (chunkContext != null && FaceLayerRouting.shouldUseCustomPass((BakedQuad) quad)) {
            var faceLayerPass = GTEmbeddiumCompat.getFaceLayerRenderPass();
            var faceLayerMaterial = GTEmbeddiumCompat.getFaceLayerMaterial();
            if (faceLayerPass != null && faceLayerMaterial != null) {
                chunkContext.buffers.get(faceLayerPass).getVertexBuffer(normalFace)
                        .push(vertices, faceLayerMaterial);
            } else {
                original.call(originalBuilder, vertices, originalMaterial);
            }
        } else {
            original.call(originalBuilder, vertices, originalMaterial);
        }

        if (chunkContext != null && BloomRenderer.usesChunkPassBackend() && BloomShaderManager.isBloomActive() &&
                ctx.renderLayer() != GTRenderTypes.bloom() &&
                TextureMetadataHelper.hasBloom((BakedQuad) quad, light.lm)) {
            var bloomBuilder = chunkContext.buffers.get(GTEmbeddiumCompat.getBloomRenderPass());

            // Bloom reuses the encoded vertices in its dedicated chunk pass.
            ChunkMeshBufferBuilder vertexBuffer = bloomBuilder.getVertexBuffer(normalFace);
            vertexBuffer.push(vertices, GTEmbeddiumCompat.getBloomMaterial());
        }
    }
}
