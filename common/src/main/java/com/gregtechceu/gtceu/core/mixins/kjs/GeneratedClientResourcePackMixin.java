package com.gregtechceu.gtceu.core.mixins.kjs;

import com.gregtechceu.gtceu.core.MixinHelpers;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.latvian.mods.kubejs.client.GeneratedClientResourcePack;
import net.minecraft.server.packs.PackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(value = GeneratedClientResourcePack.class, remap = false)
public class GeneratedClientResourcePackMixin {

    @ModifyExpressionValue(method = "inject", at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;<init>(I)V"))
    private static List<PackResources> gtceu$loadPacks(List<PackResources> resources) {
        return MixinHelpers.addDynamicResourcePack(resources);
    }
}
