package com.gregtechceu.gtceu.core.mixins.client.customchunk.sodium;

import com.gregtechceu.gtceu.client.bloom.BloomRenderer;
import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;
import com.gregtechceu.gtceu.client.renderer.FaceLayerRouting;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.integration.sodium.GTSodiumCompat;

import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AbstractBlockRenderContext;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BlockRenderer.class, remap = false)
public abstract class SodiumBlockRendererMixin extends AbstractBlockRenderContext {

    @Shadow
    private ChunkBuildBuffers buffers;

    @WrapOperation(method = "bufferQuad",
                   at = @At(value = "INVOKE",
                            target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/builder/ChunkMeshBufferBuilder;push([Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/format/ChunkVertexEncoder$Vertex;I)V"))
    private void gtceu$routeCustomPassQuads(ChunkMeshBufferBuilder originalBuilder,
                                            ChunkVertexEncoder.Vertex[] vertices, int materialBits,
                                            Operation<Void> original,
                                            MutableQuadViewImpl quad, float[] brightnesses, Material material,
                                            @Local(name = "normalFace") ModelQuadFacing normalFace) {
        if (FaceLayerRouting.isSodiumFaceLayerTag(quad.tag())) {
            var faceLayerPass = GTSodiumCompat.getFaceLayerRenderPass();
            var faceLayerMaterial = GTSodiumCompat.getFaceLayerMaterial();
            if (faceLayerPass != null && faceLayerMaterial != null) {
                this.buffers.get(faceLayerPass).getVertexBuffer(normalFace)
                        .push(vertices, faceLayerMaterial.bits());
            } else {
                original.call(originalBuilder, vertices, materialBits);
            }
        } else {
            original.call(originalBuilder, vertices, materialBits);
        }

        if (!BloomRenderer.usesChunkPassBackend() || !BloomShaderManager.isBloomActive() ||
                this.type == GTRenderTypes.bloom()) {
            return;
        }
        if (GTSodiumCompat.quadHasBloom(quad, this.quadLightData.lm)) {
            var bloomBuilder = this.buffers.get(GTSodiumCompat.getBloomRenderPass());

            // Bloom reuses the encoded vertices in the chunk pass.
            ChunkMeshBufferBuilder vertexBuffer = bloomBuilder.getVertexBuffer(normalFace);
            vertexBuffer.push(vertices, GTSodiumCompat.getBloomMaterial().bits());
        }
    }
}
