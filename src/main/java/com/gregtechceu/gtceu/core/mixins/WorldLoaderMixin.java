package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.core.MixinHelpers;

import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.WorldLoader;

import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.kubejs.util.RegistryAccessContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

// priority=1500 so this is after KubeJS has captured the registry access
@Mixin(value = WorldLoader.class, priority = 1500)
public class WorldLoaderMixin {

    @Inject(method = "load",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/server/ReloadableServerResources;loadResources(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/LayeredRegistryAccess;Lnet/minecraft/world/flag/FeatureFlagSet;Lnet/minecraft/commands/Commands$CommandSelection;ILjava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;",
                     shift = At.Shift.BEFORE))
    private static <D, R> void gtceu$postKJSVeinEvents(CallbackInfoReturnable<CompletableFuture<R>> cir,
                                                       // @Local(ordinal = 0) RegistryAccess.Frozen
                                                       // registriesWithWorldgen,
                                                       // @Local(ordinal = 1) RegistryAccess.Frozen
                                                       // registriesWithDimensions,
                                                       @Local(ordinal = 1) LayeredRegistryAccess<RegistryLayer> layered) {
        RegistryAccess.Frozen registriesWithEverything = layered.compositeAccess();
        if (GTCEu.Mods.isKubeJSLoaded()) {
            if (RegistryAccessContainer.current.access().registries().count() <
                    registriesWithEverything.registries().count()) {
                RegistryAccessContainer.current = new RegistryAccessContainer(registriesWithEverything);
            }
        }

        MixinHelpers.postKJSVeinEvents(registriesWithEverything);
    }
}
