package com.gregtechceu.gtceu.core.mixins.client.bloom.safemode;

import com.gregtechceu.gtceu.client.bloom.BloomSafeMode;
import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "compileChunks",
            at = @At(value = "INVOKE",
                    ordinal = 0,
                    target = "Lnet/minecraft/world/level/lighting/LevelLightEngine;lightOnInSection(Lnet/minecraft/core/SectionPos;)Z"))
    private void gtceu$compileBloomBuffers(Camera camera, CallbackInfo ci,
                                           @Local SectionPos sectionPos) {
        BloomSafeMode.CURRENT_RENDERING_SECTION.set(sectionPos);
        BloomSafeMode.bakeBloomChunkBuffers(sectionPos, camera.getPosition());
    }
}
