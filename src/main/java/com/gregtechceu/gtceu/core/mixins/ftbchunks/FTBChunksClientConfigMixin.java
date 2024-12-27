package com.gregtechceu.gtceu.core.mixins.ftbchunks;

import com.gregtechceu.gtceu.integration.map.ftbchunks.FTBChunksOptions;

import dev.ftb.mods.ftbchunks.client.FTBChunksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FTBChunksClient.class, remap = false)
public abstract class FTBChunksClientConfigMixin {

    @Inject(method = "init",
            at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbchunks/client/FTBChunksClientConfig;init()V"))
    public void gtceu$injectSaveConfig(CallbackInfo ci) {
        FTBChunksOptions.initialize();
    }
}
