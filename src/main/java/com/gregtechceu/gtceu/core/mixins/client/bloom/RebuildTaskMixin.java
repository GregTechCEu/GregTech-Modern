package com.gregtechceu.gtceu.core.mixins.client.bloom;

import com.gregtechceu.gtceu.client.bloom.BloomUtil;

import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.gregtechceu.gtceu.config.ConfigHolder;
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
public abstract class RebuildTaskMixin {

    @Shadow(aliases = { "this$1", "f_290687_", "f" })
    @Final
    ChunkRenderDispatcher.RenderChunk this$1;

    @SuppressWarnings("NameDoesntMatchTargetClass")
    @Inject(method = "compile",
            at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;", remap = false))
    private void gtceu$tryAddBlockEntity(float x, float y, float z, ChunkBufferBuilderPack chunkBufferBuilders,
                                         CallbackInfoReturnable<Object> cir,
                                         @Local(ordinal = 0) BlockPos sectionOrigin,
                                         @Local Set<RenderType> usedRenderTypes) {
        if (ConfigHolder.INSTANCE.client.bloom.safeMode) return;
        if (!GTShaders.canUseBloomShader()) return;

        long sectionPos = SectionPos.asLong(sectionOrigin);
        if (!BloomUtil.chunkSectionHasBloomQuads(sectionPos)) return;

        BloomUtil.drawBlockBloomForChunk(sectionPos, renderType -> {
            BufferBuilder buffer = chunkBufferBuilders.builder(renderType);
            // no existing geometry on this layer
            if (usedRenderTypes.add(renderType)) this$1.beginLayer(buffer);

            return buffer;
        });
    }
}
