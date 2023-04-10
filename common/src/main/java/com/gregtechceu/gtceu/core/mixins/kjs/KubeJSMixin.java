package com.gregtechceu.gtceu.core.mixins.kjs;

import com.gregtechceu.gtceu.common.CommonProxy;
import dev.latvian.mods.kubejs.KubeJS;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author KilaBash
 * @date 2023/3/26
 * @implNote KubeJSMixin
 */
@Mixin(value = KubeJS.class, remap = false)
public abstract class KubeJSMixin {

    /**
     * Make sure our mod is loaded after kjs
     */
    @Inject(method = "setup", at = @At(value = "RETURN"), remap = false)
    public void gtceu$injectInit(CallbackInfo ci) {
        //CommonProxy.onKubeJSSetup();
    }

}
