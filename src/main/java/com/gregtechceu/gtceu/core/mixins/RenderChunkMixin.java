package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.client.util.BloomEffectUtil;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkRenderDispatcher.RenderChunk.class)
public class RenderChunkMixin {

    @Shadow @Final
    BlockPos.MutableBlockPos origin;

    @Inject(method = "releaseBuffers", at = @At("HEAD"))
    private void gtceu$releaseBloomBuffers(CallbackInfo ci) {
        BloomEffectUtil.removeBloomChunk(this.origin);
    }
}
