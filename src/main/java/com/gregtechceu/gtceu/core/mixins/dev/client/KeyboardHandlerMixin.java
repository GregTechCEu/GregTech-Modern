package com.gregtechceu.gtceu.core.mixins.dev.client;

import com.gregtechceu.gtceu.utils.dev.ResourceReloadDetector;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

    @WrapOperation(method = "handleDebugKeys",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/Minecraft;reloadResourcePacks()Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<Void> gtceu$hookResourceReload(Minecraft instance,
                                                             Operation<CompletableFuture<Void>> original) {
        return ResourceReloadDetector.regenerateResourcesOnReload(() -> original.call(instance));
    }
}
