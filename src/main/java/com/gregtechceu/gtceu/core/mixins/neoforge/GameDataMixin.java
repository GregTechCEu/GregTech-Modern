package com.gregtechceu.gtceu.core.mixins.neoforge;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.GameData;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.LinkedHashSet;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = GameData.class, remap = false)
public class GameDataMixin {

    @ModifyExpressionValue(method = "getRegistrationOrder", at = @At(value = "NEW", target = "java/util/LinkedHashSet"))
    private static LinkedHashSet<Identifier> gtceu$injectGTRegistriesFirst(LinkedHashSet<Identifier> ordered) {
        ordered.addAll(GTRegistries.getRegistrationOrder());
        return ordered;
    }
}
