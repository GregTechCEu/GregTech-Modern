package com.gregtechceu.gtceu.core.mixins.client.bloom.safemode;

import com.gregtechceu.gtceu.client.bloom.BloomSafeMode;

import com.gregtechceu.gtceu.config.ConfigHolder;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;

import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Safe mode version of {@link com.gregtechceu.gtceu.core.mixins.client.bloom.RebuildTaskMixin}
 *
 * @see com.gregtechceu.gtceu.core.mixins.client.bloom.RebuildTaskMixin
 */
@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask")
public class RebuildTaskMixin {

    @Shadow(aliases = { "this$1", "f_290687_", "f" })
    @Final
    ChunkRenderDispatcher.RenderChunk this$1;

    @Inject(method = "compile", at = @At(value = "HEAD"))
    private void gtceu$startBloomBufferForChunk(float x, float y, float z,
                                                ChunkBufferBuilderPack chunkBufferBuilderPack,
                                                CallbackInfoReturnable<Object> cir) {
        if (!ConfigHolder.INSTANCE.client.bloom.safeMode) return;

        BlockPos pos = this.this$1.getOrigin();
        BloomSafeMode.CURRENT_RENDERING_SECTION.set(SectionPos.of(pos));
    }
}
