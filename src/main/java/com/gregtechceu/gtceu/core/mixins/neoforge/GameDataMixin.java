package com.gregtechceu.gtceu.core.mixins.neoforge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.kjs.helpers.KubeGTRegistryEventHandler;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.registries.GameData;
import net.neoforged.neoforge.registries.RegisterEvent;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.LinkedHashSet;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = GameData.class, remap = false)
public class GameDataMixin {

    @ModifyExpressionValue(method = "getRegistrationOrder", at = @At(value = "NEW", target = "java/util/LinkedHashSet"))
    private static LinkedHashSet<ResourceLocation> gtceu$injectGTRegistriesFirst(LinkedHashSet<ResourceLocation> ordered) {
        ordered.addAll(GTRegistries.getRegistryOrder());
        return ordered;
    }

    @WrapOperation(method = "postRegisterEvents",
                   at = @At(value = "INVOKE",
                            target = "Lnet/neoforged/fml/ModLoader;postEventWrapContainerInModOrder(Lnet/neoforged/bus/api/Event;)V"))
    private static void gtceu$registerRecipeTypeAliases(Event event, Operation<Void> original) {
        original.call(event);
        // Alias scripts must run after every listener for the real registry, including LOWEST listeners.
        if (!ModLoader.hasErrors() && event instanceof RegisterEvent registerEvent &&
                registerEvent.getRegistryKey().equals(Registries.RECIPE_TYPE) && GTCEu.Mods.isKubeJSLoaded()) {
            KubeGTRegistryEventHandler.registerRecipeTypes(registerEvent);
        }
    }
}
