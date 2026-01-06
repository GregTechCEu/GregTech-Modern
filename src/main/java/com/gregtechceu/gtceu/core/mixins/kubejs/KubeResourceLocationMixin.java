package com.gregtechceu.gtceu.core.mixins.kubejs;

import com.gregtechceu.gtceu.integration.kjs.ImplicitKubeResourceLocation;

import dev.latvian.mods.kubejs.util.KubeResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = KubeResourceLocation.class)
public class KubeResourceLocationMixin {

    @Inject(at = @At("HEAD"), method = "wrap", cancellable = true)
    private static void wrap(Object args, CallbackInfoReturnable<KubeResourceLocation> cir) {
        if (args instanceof String stringArg) {
            if (!stringArg.contains(":")) {
                cir.setReturnValue(new KubeResourceLocation(ImplicitKubeResourceLocation.of(stringArg)));
            }
        }
    }
}
