package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.MixinHelpers;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {

    /// this is called after data load and before registry freeze
    @Inject(method = "load(Lnet/minecraft/resources/RegistryDataLoader$LoadingFunction;Lnet/minecraft/core/RegistryAccess;Ljava/util/List;)Lnet/minecraft/core/RegistryAccess$Frozen;",
            at = @At(value = "INVOKE",
                     target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V",
                     ordinal = 1,
                     remap = false))
    private static void gtceu$postKJSVeinEvents(RegistryDataLoader.LoadingFunction loadingFunction,
                                                RegistryAccess registryAccess,
                                                List<RegistryDataLoader.RegistryData<?>> registryData,
                                                CallbackInfoReturnable<RegistryAccess.Frozen> cir,
                                                @Local(ordinal = 1) List<RegistryDataLoader.Loader<?>> loaders) {
        MixinHelpers.updateCachedRegistryAndPostKJSVeinEvents(registryAccess, loaders);
    }
}
