package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.WorldLoader;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(value = WorldLoader.class)
public class WorldLoaderMixin {

    @Inject(method = "load",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/server/ReloadableServerResources;loadResources(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/RegistryAccess$Frozen;Lnet/minecraft/world/flag/FeatureFlagSet;Lnet/minecraft/commands/Commands$CommandSelection;ILjava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private static <D, R> void gtceu$injectLoad(WorldLoader.InitConfig initConfig,
                                                WorldLoader.WorldDataSupplier<D> loadContextSupplier,
                                                WorldLoader.ResultFactory<D, R> applierFactory,
                                                Executor prepareExecutor, Executor applyExecutor,
                                                CallbackInfoReturnable<CompletableFuture<R>> cir,
                                                @Local(ordinal = 2) RegistryAccess.Frozen frozen2) {
        GTCEu.updateFrozenRegistry(frozen2);
    }
}
