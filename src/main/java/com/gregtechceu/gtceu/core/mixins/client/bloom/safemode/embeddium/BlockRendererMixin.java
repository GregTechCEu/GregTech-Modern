package com.gregtechceu.gtceu.core.mixins.client.bloom.safemode.embeddium;

import com.gregtechceu.gtceu.client.bloom.BloomRenderer;
import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;
import com.gregtechceu.gtceu.client.util.TextureMetadataHelper;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.embeddedt.embeddium.api.render.chunk.BlockRenderContext;
import org.embeddedt.embeddium.api.util.ColorARGB;
import org.embeddedt.embeddium.api.util.NormI8;
import org.embeddedt.embeddium.impl.model.light.data.QuadLightData;
import org.embeddedt.embeddium.impl.model.quad.BakedQuadView;
import org.embeddedt.embeddium.impl.render.chunk.compile.buffers.ChunkModelBuilder;
import org.embeddedt.embeddium.impl.render.chunk.compile.pipeline.BlockRenderer;
import org.embeddedt.embeddium.impl.render.chunk.terrain.material.Material;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexEncoder;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Safe mode version of {@link com.gregtechceu.gtceu.core.mixins.client.bloom.normal.embeddium.BlockRendererMixin}
 *
 * @see com.gregtechceu.gtceu.core.mixins.client.bloom.normal.embeddium.BlockRendererMixin
 */
@Mixin(value = BlockRenderer.class, remap = false)
public class BlockRendererMixin {

    @Inject(method = "writeGeometry", at = @At(value = "HEAD"))
    private void gtceu$copyBloomQuads$initLocals(BlockRenderContext ctx, ChunkModelBuilder builder,
                                                 Vec3 offset, Material material, BakedQuadView quad,
                                                 int[] colors, QuadLightData lightData,
                                                 CallbackInfo ci,
                                                 @Share("bloomBuffer") LocalRef<VertexConsumer> bloomBufferRef) {
        // Check if quad is full brightness OR we have bloom enabled for the quad
        if (BloomShaderManager.isBloomActive() && TextureMetadataHelper.hasBloom((BakedQuad) quad, lightData.lm)) {
            SectionPos sectionPos = SectionPos.of(ctx.pos());
            bloomBufferRef.set(BloomRenderer.SafeMode.getOrStartBloomBuffer(sectionPos));
        } else {
            bloomBufferRef.set(null);
        }
    }

    @Inject(method = "writeGeometry",
            at = @At(value = "FIELD",
                     target = "Lorg/embeddedt/embeddium/impl/render/chunk/vertex/format/ChunkVertexEncoder$Vertex;light:I",
                     opcode = Opcodes.PUTFIELD,
                     shift = At.Shift.AFTER))
    private void gtceu$copyBloomQuads(BlockRenderContext ctx, ChunkModelBuilder builder, Vec3 offset,
                                      Material material, BakedQuadView quad, int[] colors, QuadLightData light,
                                      CallbackInfo ci,
                                      @Local(name = "srcIndex") int srcIndex,
                                      @Local(name = "out") ChunkVertexEncoder.Vertex v,
                                      @Share("bloomBuffer") LocalRef<VertexConsumer> bloomBufferRef) {
        VertexConsumer bloomBuffer = bloomBufferRef.get();
        // bloomBuffer is null if bloom isn't available or the quad's texture doesn't have bloom
        if (bloomBuffer == null) return;

        int normal = quad.getForgeNormal(srcIndex);
        if (normal == 0) normal = quad.getComputedFaceNormal();

        bloomBuffer.addVertex(v.x, v.y, v.z)
                .setColor(ColorARGB.toABGR(v.color))
                .setUv(v.u, v.v)
                .setLight(v.light)
                .setNormal(NormI8.unpackX(normal), NormI8.unpackY(normal), NormI8.unpackZ(normal));
    }
}
