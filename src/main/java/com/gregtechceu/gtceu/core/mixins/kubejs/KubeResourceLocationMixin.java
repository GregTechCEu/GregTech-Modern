package com.gregtechceu.gtceu.core.mixins.kubejs;

import com.gregtechceu.gtceu.core.IResourceLocationExtensions;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.util.KubeResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = KubeResourceLocation.class)
public abstract class KubeResourceLocationMixin {

    @Inject(at = @At("HEAD"), method = "wrap", cancellable = true)
    private static void wrap(Object args, CallbackInfoReturnable<KubeResourceLocation> cir) {
        if (args instanceof String stringArg) {
            if (!stringArg.contains(":")) {
                var loc = ResourceLocation.fromNamespaceAndPath("kubejs", stringArg);
                ((IResourceLocationExtensions) (Object) loc).gtm$setImplicit(true);
                cir.setReturnValue(new KubeResourceLocation(loc));
            }
        }
    }
}
