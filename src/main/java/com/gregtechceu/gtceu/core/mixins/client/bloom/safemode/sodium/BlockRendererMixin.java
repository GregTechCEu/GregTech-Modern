package com.gregtechceu.gtceu.core.mixins.client.bloom.safemode.sodium;

import com.gregtechceu.gtceu.client.bloom.BloomRenderer;
import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;
import com.gregtechceu.gtceu.integration.sodium.GTSodiumCompat;

import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AbstractBlockRenderContext;
import net.minecraft.core.SectionPos;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
public abstract class BlockRendererMixin extends AbstractBlockRenderContext {

    @Inject(method = "processQuad",
            at = @At(value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;shadeQuad(Lnet/caffeinemc/mods/sodium/client/render/frapi/mesh/MutableQuadViewImpl;Lnet/caffeinemc/mods/sodium/client/model/light/LightMode;ZLnet/fabricmc/fabric/api/renderer/v1/material/ShadeMode;)V",
                    shift = At.Shift.AFTER))
    private void gtceu$copyBloomQuads$initLocals(MutableQuadViewImpl quad,
                                                 CallbackInfo ci,
                                                 @Local(name = "emissive") boolean emissive,
                                                 @Share("bloomBuffer") LocalRef<VertexConsumer> bloomBufferRef) {
        // Check if quad is full brightness OR we have bloom enabled for the quad
        if (BloomShaderManager.isBloomActive() && GTSodiumCompat.quadHasBloom(quad, this.quadLightData.lm, emissive)) {
            SectionPos sectionPos = SectionPos.of(this.pos);
            bloomBufferRef.set(BloomRenderer.SafeMode.getOrStartBloomBuffer(sectionPos));
        } else {
            bloomBufferRef.set(null);
        }
    }

    @Inject(method = "bufferQuad",
            at = @At(value = "FIELD",
                     target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/format/ChunkVertexEncoder$Vertex;light:I",
                     opcode = Opcodes.PUTFIELD,
                     shift = At.Shift.AFTER))
    private void gtceu$copyBloomQuads(MutableQuadViewImpl quad, float[] brightnesses, Material material,
                                      CallbackInfo ci,
                                      @Local(name = "srcIndex") int srcIndex,
                                      @Local(name = "out") ChunkVertexEncoder.Vertex out,
                                      @Share("bloomBuffer") LocalRef<VertexConsumer> bloomBufferRef) {
        VertexConsumer bloomBuffer = bloomBufferRef.get();
        // bloomBuffer is null if bloom isn't available or the quad's texture doesn't have bloom
        if (bloomBuffer == null) return;

        int normal = quad.getAccurateNormal(srcIndex);

        bloomBuffer.addVertex(out.x, out.y, out.z)
                .setColor(ColorARGB.toABGR(out.color))
                .setUv(out.u, out.v)
                .setLight(out.light)
                .setNormal(NormI8.unpackX(normal), NormI8.unpackY(normal), NormI8.unpackZ(normal));
    }
}
