package com.gregtechceu.gtceu.core.mixins.client.bloom;

import com.gregtechceu.gtceu.client.bloom.BloomUtil;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;

import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask")
abstract class RebuildTaskMixin {

    @Shadow(aliases = { "this$1", "f_290687_", "f" })
    @Final
    ChunkRenderDispatcher.RenderChunk this$1;

    /*
    @Definition(id = "chunkBufferBuilderPack", local = @Local(type = ChunkBufferBuilderPack.class, argsOnly = true))
    @Definition(id = "builder", method = "Lnet/minecraft/client/renderer/ChunkBufferBuilderPack;builder(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/BufferBuilder;")
    @Definition(id = "endOrDiscardIfEmpty", method = "Lcom/mojang/blaze3d/vertex/BufferBuilder;endOrDiscardIfEmpty()Lcom/mojang/blaze3d/vertex/BufferBuilder$RenderedBuffer;")
    @Definition(id = "renderType1", local = @Local(type = RenderType.class))
    @Expression("@(chunkBufferBuilderPack.builder(renderType1)).endOrDiscardIfEmpty()")
    @SuppressWarnings("NameDoesntMatchTargetClass")
    @ModifyExpressionValue(method = "compile", at = @At("MIXINEXTRAS:EXPRESSION"))
    private BufferBuilder gtceu$tryAddBlockEntity(BufferBuilder buffer,
                                                  @Local(ordinal = 0) BlockPos sectionOrigin,
                                                  @Local(ordinal = 0) RenderType renderType) {
        if (renderType != GTRenderTypes.bloom()) return buffer;

        long sectionPos = SectionPos.asLong(sectionOrigin);
        if (BloomUtil.chunkSectionHasBloomQuads(sectionPos)) {
            BloomUtil.drawBlockBloomForChunk(sectionPos, buffer);
        }

        return buffer;
    }
    */

    @SuppressWarnings("NameDoesntMatchTargetClass")
    @Inject(method = "compile",
            at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;", remap = false))
    private void gtceu$tryAddBlockEntity(float x, float y, float z, ChunkBufferBuilderPack chunkBufferBuilders,
                                         CallbackInfoReturnable<Object> cir,
                                         @Local(ordinal = 0) BlockPos sectionOrigin,
                                         @Local Set<RenderType> usedRenderTypes) {
        long sectionPos = SectionPos.asLong(sectionOrigin);
        if (BloomUtil.chunkSectionHasBloomQuads(sectionPos)) {
            BufferBuilder bloomBuffer = chunkBufferBuilders.builder(GTRenderTypes.bloom());
            // no existing geometry on this layer
            if (usedRenderTypes.add(GTRenderTypes.bloom())) this$1.beginLayer(bloomBuffer);

            BufferBuilder cutoutBuffer = chunkBufferBuilders.builder(RenderType.cutout());
            if (usedRenderTypes.add(RenderType.cutout())) this$1.beginLayer(cutoutBuffer);

            BloomUtil.drawBlockBloomForChunk(sectionPos, bloomBuffer, cutoutBuffer);
        }
    }
}
