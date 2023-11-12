package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.MixinHelpers;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @ModifyExpressionValue(
            method = {"reloadResourcePacks(Z)Ljava/util/concurrent/CompletableFuture;", "<init>"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;openAllSelected()Ljava/util/List;")
    )
    private List<PackResources> gtceu$loadPacks(List<PackResources> resources) {
        return MixinHelpers.addDynamicResourcePack(resources);
    }
}
