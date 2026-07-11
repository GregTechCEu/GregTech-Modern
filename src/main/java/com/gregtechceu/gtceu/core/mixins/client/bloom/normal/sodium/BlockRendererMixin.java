package com.gregtechceu.gtceu.core.mixins.client.bloom.normal.sodium;

import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;
import com.gregtechceu.gtceu.integration.sodium.GTSodiumCompat;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AbstractBlockRenderContext;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockRenderer.class, remap = false)
public abstract class BlockRendererMixin extends AbstractBlockRenderContext {

    @Shadow
    protected abstract void bufferQuad(MutableQuadViewImpl quad, float[] brightnesses, Material material);

    @Shadow
    private ChunkBuildBuffers buffers;

    @Inject(method = "processQuad",
            at = @At(value = "INVOKE",
                     target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;bufferQuad(Lnet/caffeinemc/mods/sodium/client/render/frapi/mesh/MutableQuadViewImpl;[FLnet/caffeinemc/mods/sodium/client/render/chunk/terrain/material/Material;)V",
                     shift = At.Shift.AFTER))
    private void gtceu$copyBloomQuads(MutableQuadViewImpl quad,
                                      CallbackInfo ci,
                                      @Local(name = "emissive") boolean emissive,
                                      @Share("bloomBuilder") LocalRef<ChunkModelBuilder> bloomBuilderRef) {
        if (BloomShaderManager.isBloomActive() && GTSodiumCompat.quadHasBloom(quad, this.quadLightData.lm, emissive)) {
            var bloomBuilder = this.buffers.get(GTSodiumCompat.BLOOM_RENDER_PASS);
            bloomBuilderRef.set(bloomBuilder);

            // call the same method again, this time with the bloom chunk model builder
            this.bufferQuad(quad, this.quadLightData.br, GTSodiumCompat.BLOOM_MATERIAL);
        } else {
            bloomBuilderRef.set(null);
        }
    }

    @Inject(method = "bufferQuad",
            at = @At(value = "INVOKE",
                     target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/buffers/ChunkModelBuilder;addSprite(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V",
                     shift = At.Shift.AFTER))
    private void gtceu$copyBloomSpriteAdds(MutableQuadViewImpl quad, float[] brightnesses, Material material,
                                           CallbackInfo ci,
                                           @Local(name = "atlasSprite") TextureAtlasSprite atlasSprite,
                                           @Share("bloomBuilder") LocalRef<ChunkModelBuilder> bloomBuilderRef) {
        // set by the above inject; value is only non-null when all appropriate conditions/requirements apply.
        // thus no need to check them here.
        ChunkModelBuilder bloomBuilder = bloomBuilderRef.get();
        if (bloomBuilder != null) {
            bloomBuilder.addSprite(atlasSprite);
        }
    }
}
