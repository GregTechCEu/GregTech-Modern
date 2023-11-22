package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.core.MixinHelpers;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @ModifyExpressionValue(
            method = {"reloadResourcePacks(Z)Ljava/util/concurrent/CompletableFuture;", "<init>"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;openAllSelected()Ljava/util/List;")
    )
    private List<PackResources> gtceu$loadPacks(List<PackResources> resources) {
        return MixinHelpers.addDynamicResourcePack(resources);
    }

    @Inject(method = "reloadResourcePacks(Z)Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"))
    private void gtceu$invalidatePipeModelCaches(boolean force, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        PipeModel.invalidateAllCachedModels();
    }
}
