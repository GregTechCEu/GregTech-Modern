package com.gregtechceu.gtceu.core.mixins.neoforge;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.GameData;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.LinkedHashSet;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = GameData.class, remap = false)
public class GameDataMixin {

    @ModifyExpressionValue(method = "getRegistrationOrder", at = @At(value = "NEW", target = "java/util/LinkedHashSet"))
    private static LinkedHashSet<ResourceLocation> gtceu$injectGTRegistriesFirst(LinkedHashSet<ResourceLocation> ordered) {
        ordered.addAll(BuiltInRegistries.REGISTRY.keySet().stream().filter(r -> r.getNamespace().equals(GTCEu.MOD_ID))
                .sorted(ResourceLocation::compareNamespaced).toList());
        return ordered;
    }
}
