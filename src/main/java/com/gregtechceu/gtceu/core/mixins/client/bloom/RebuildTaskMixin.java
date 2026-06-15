package com.gregtechceu.gtceu.core.mixins.client.bloom;

import com.gregtechceu.gtceu.client.bloom.BloomRenderer;
import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;

import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.function.Supplier;

@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask")
public abstract class RebuildTaskMixin {

    @Shadow(aliases = { "this$1", "f_290687_", "f" })
    @Final
    ChunkRenderDispatcher.RenderChunk this$1;

    @Inject(method = "compile",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/Minecraft;getBlockRenderer()Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;"))
    private void gtceu$initBloomContextData(float camX, float camY, float camZ, ChunkBufferBuilderPack builders,
                                            CallbackInfoReturnable<Object> cir,
                                            @Local(ordinal = 0) BlockPos sectionOrigin,
                                            @Local Set<RenderType> usedRenderTypes) {
        if (!BloomShaderManager.isBloomActive()) return;

        Supplier<VertexConsumer> provider = () -> {
            if (!BloomRenderer.SafeMode.enabled()) {
                BufferBuilder buffer = builders.builder(GTRenderTypes.bloom());
                // no existing geometry on this layer
                if (usedRenderTypes.add(GTRenderTypes.bloom())) this$1.beginLayer(buffer);
                return buffer;
            } else {
                // safe mode path
                return BloomRenderer.SafeMode.getOrStartBloomBuffer(SectionPos.of(sectionOrigin));
            }
        };
        // intentionally no try-with-resource statement; closed in 'gtceu$clearBloomContextData'
        BloomRenderer.bloomChunkContext().get().with(provider);
    }

    @Inject(method = "compile",
            at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;", remap = false))
    private void gtceu$clearBloomContextData(float camX, float camY, float camZ, ChunkBufferBuilderPack builders,
                                             CallbackInfoReturnable<Object> cir,
                                             @Local(ordinal = 0) BlockPos sectionOrigin) {
        if (!BloomShaderManager.isBloomActive()) return;

        if (BloomRenderer.SafeMode.enabled()) {
            BloomRenderer.SafeMode.bakeBloomChunkBuffers(SectionPos.of(sectionOrigin), camX, camY, camZ);
        }

        BloomRenderer.bloomChunkContext().get().close();
    }
}
