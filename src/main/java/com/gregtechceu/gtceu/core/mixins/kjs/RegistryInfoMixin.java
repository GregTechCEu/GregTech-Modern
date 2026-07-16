package com.gregtechceu.gtceu.core.mixins.kjs;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.kjs.helpers.GTRegistryInfo;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import dev.latvian.mods.kubejs.registry.RegistryCallback;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RegistryInfo.class, remap = false)
public abstract class RegistryInfoMixin<T> {

    @Final
    @Shadow
    public ResourceKey<? extends Registry<T>> key;

    @Inject(method = "registerObjects", at = @At("HEAD"), cancellable = true)
    public void gtceu$registerObjects(RegistryCallback<T> function, CallbackInfoReturnable<Integer> cir) {
        if (key.location().getNamespace().equals(GTCEu.MOD_ID)) {
            RegistryInfo<T> self = (RegistryInfo<T>) (Object) this;
            var count = GTRegistryInfo.registerObjects(self, function);
            cir.setReturnValue(count);
        }
    }
}
