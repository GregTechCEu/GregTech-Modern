package com.gregtechceu.gtceu.core.mixins.client.bloom.normal.sodium;

import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;
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

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockRenderer.class, remap = false)
public abstract class BlockRendererMixin extends AbstractBlockRenderContext {

    @Shadow
    private ChunkBuildBuffers buffers;

    @Inject(method = "bufferQuad",
            at = @At(value = "INVOKE",
                     target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/builder/ChunkMeshBufferBuilder;push([Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/format/ChunkVertexEncoder$Vertex;I)V",
                     shift = At.Shift.AFTER))
    private void gtceu$copyBloomQuads(MutableQuadViewImpl quad, float[] brightnesses, Material material,
                                      CallbackInfo ci,
                                      @Local(name = "vertices") ChunkVertexEncoder.Vertex[] vertices,
                                      @Local(name = "normalFace") ModelQuadFacing normalFace) {
        if (!BloomShaderManager.isBloomActive() || this.type == GTRenderTypes.bloom()) {
            return;
        }
        if (GTSodiumCompat.quadHasBloom(quad, this.quadLightData.lm)) {
            var bloomBuilder = this.buffers.get(GTSodiumCompat.getBloomRenderPass());

            // call the same method again, this time with the bloom chunk model builder
            ChunkMeshBufferBuilder vertexBuffer = bloomBuilder.getVertexBuffer(normalFace);
            vertexBuffer.push(vertices, GTSodiumCompat.getBloomMaterial().bits());
        }
    }
}
