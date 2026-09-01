package com.gregtechceu.gtceu.core.mixins.client.bloom;

import com.gregtechceu.gtceu.client.bloom.BloomRenderer;
import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;

import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.SectionPos;

import com.mojang.blaze3d.vertex.VertexSorting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SectionCompiler.class)
public abstract class SectionCompilerMixin {

    @Inject(method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/util/RandomSource;create()Lnet/minecraft/util/RandomSource;"))
    private void gtceu$initBloomContextData(SectionPos sectionPos, RenderChunkRegion region,
                                            VertexSorting vertexSorting, SectionBufferBuilderPack builders,
                                            List<?> additionalRenderers,
                                            CallbackInfoReturnable<SectionCompiler.Results> cir) {
        if (!BloomShaderManager.isBloomActive()) return;

        // intentionally no 'try'-with-resources statement; closed in 'gtceu$clearBloomContextData'
        BloomRenderer.bloomChunkContext().with(() -> BloomRenderer.SafeMode.getOrStartBloomBuffer(sectionPos));
    }

    @Inject(method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;",
            at = @At("RETURN"))
    private void gtceu$clearBloomContextData(SectionPos sectionPos, RenderChunkRegion region,
                                             VertexSorting vertexSorting, SectionBufferBuilderPack builders,
                                             List<?> additionalRenderers,
                                             CallbackInfoReturnable<SectionCompiler.Results> cir) {
        if (!BloomShaderManager.isBloomActive()) return;

        BloomRenderer.SafeMode.bakeBloomChunkBuffers(sectionPos, vertexSorting);

        BloomRenderer.bloomChunkContext().close();
    }
}
