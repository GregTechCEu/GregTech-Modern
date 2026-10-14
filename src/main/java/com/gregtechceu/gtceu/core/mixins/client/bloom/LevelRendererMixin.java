package com.gregtechceu.gtceu.core.mixins.client.bloom;

import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;

import net.minecraft.client.renderer.LevelRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Inject(method = "resize", at = @At("TAIL"))
    private void gtceu$resizeBloomChain(int width, int height, CallbackInfo ci) {
        if (BloomShaderManager.BLOOM_CHAIN != null) {
            BloomShaderManager.BLOOM_CHAIN.resize(width, height);
        }
    }

    @Inject(method = "graphicsChanged", at = @At(value = "HEAD"))
    private void gtceu$reinitBloomEffect(CallbackInfo ci) {
        BloomShaderManager.initPostShaders();
    }
}
