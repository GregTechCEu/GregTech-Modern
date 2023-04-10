package com.gregtechceu.gtceu.fabric.core.mixins.kjs;

import com.gregtechceu.gtceu.common.CommonProxy;
import dev.latvian.mods.kubejs.fabric.KubeJSFabric;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KubeJSFabric.class)
public class KubeJSFabricMixin {

    @Inject(remap = false, method = "onInitialize()V", at = @At(value = "RETURN"))
    public void gtceu$init(CallbackInfo ci) {
        CommonProxy.init();
    }
}
