package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.data.dynamic.DynamicLootHandler;

import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(value = ReloadableServerResources.class, priority = 2000)
public abstract class ReloadableServerResourcesMixin {

    @Inject(method = "loadResources", at = @At("HEAD"))
    private static void gtceu$init(ResourceManager resourceManager, LayeredRegistryAccess<RegistryLayer> access,
                                   FeatureFlagSet featureFlags, Commands.CommandSelection commands,
                                   int functionCompilationLevel, Executor backgroundExecutor, Executor gameExecutor,
                                   CallbackInfoReturnable<CompletableFuture<ReloadableServerResources>> cir) {
        // load loot tables *before* other data so we have the registries loaded before saving recipes to JSON.
        // because it breaks if we don't do that.

        // this doesn't have reloadable registries available, by the way.
        RegistryAccess.Frozen registries = access.compositeAccess();

        // Register dynamic loot
        DynamicLootHandler.generateDynamicLoot(registries);
    }
}
