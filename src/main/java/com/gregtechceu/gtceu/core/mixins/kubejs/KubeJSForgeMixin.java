package com.gregtechceu.gtceu.core.mixins.kubejs;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import dev.latvian.mods.kubejs.forge.KubeJSForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Supplier;

@Mixin(value = KubeJSForge.class, remap = false)
public class KubeJSForgeMixin {

    @Redirect(method = "<init>",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraftforge/fml/DistExecutor;safeRunWhenOn(Lnet/minecraftforge/api/distmarker/Dist;Ljava/util/function/Supplier;)V"))
    private void gtceu$unsafeRunWhenOnPleaseAndThankYou(Dist dist, Supplier<DistExecutor.SafeRunnable> toRun) {
        DistExecutor.unsafeRunWhenOn(dist, () -> () -> toRun.get().run());
    }
}
