package com.gregtechceu.gtceu.core.mixins.client.bloom;

import com.gregtechceu.gtceu.client.bloom.BloomRenderer;
import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;

import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask")
public abstract class RebuildTaskMixin {

    @Inject(method = "compile",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/Minecraft;getBlockRenderer()Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;"))
    private void gtceu$initBloomContextData(float camX, float camY, float camZ, ChunkBufferBuilderPack builders,
                                            CallbackInfoReturnable<Object> cir,
                                            @Local(ordinal = 0) BlockPos sectionOrigin) {
        if (!BloomShaderManager.isBloomActive()) return;

        // intentionally no 'try'-with-resources statement; closed in 'gtceu$clearBloomContextData'
        // noinspection resource
        BloomRenderer.bloomChunkContext()
                .with(() -> BloomRenderer.SafeMode.getOrStartBloomBuffer(SectionPos.of(sectionOrigin)));
    }

    @Inject(method = "compile", at = @At("RETURN"))
    private void gtceu$clearBloomContextData(float camX, float camY, float camZ, ChunkBufferBuilderPack builders,
                                             CallbackInfoReturnable<Object> cir,
                                             @Local(ordinal = 0) BlockPos sectionOrigin) {
        if (!BloomShaderManager.isBloomActive()) return;

        BloomRenderer.SafeMode.bakeBloomChunkBuffers(SectionPos.of(sectionOrigin), camX, camY, camZ);

        BloomRenderer.bloomChunkContext().close();
    }
}
