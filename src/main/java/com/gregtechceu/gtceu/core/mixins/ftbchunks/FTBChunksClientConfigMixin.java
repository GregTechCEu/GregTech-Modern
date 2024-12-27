package com.gregtechceu.gtceu.core.mixins.ftbchunks;

import com.gregtechceu.gtceu.integration.map.ftbchunks.FTBChunksOptions;

import dev.ftb.mods.ftbchunks.client.FTBChunksClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FTBChunksClientConfig.class, remap = false)
public interface FTBChunksClientConfigMixin {

    @Inject(method = "<clinit>",
            at = @At(value = "INVOKE",
                     shift = At.Shift.AFTER,
                     target = "Ldev/ftb/mods/ftblibrary/snbt/config/SNBTConfig;create(Ljava/lang/String;)Ldev/ftb/mods/ftblibrary/snbt/config/SNBTConfig;"))
    private static void gtceu$injectSaveConfig(CallbackInfo ci) {
        FTBChunksOptions.initialize();
    }
}
