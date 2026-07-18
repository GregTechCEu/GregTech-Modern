package com.gregtechceu.gtceu.core.mixins.neoforge;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.GameData;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.LinkedHashSet;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = GameData.class, remap = false)
public class GameDataMixin {

    @ModifyExpressionValue(method = "getRegistrationOrder", at = @At(value = "NEW", target = "java/util/LinkedHashSet"))
    private static LinkedHashSet<ResourceLocation> gtceu$injectGTRegistriesFirst(LinkedHashSet<ResourceLocation> ordered) {
        ordered.addAll(GTRegistries.getRegistryOrder());
        return ordered;
    }

    /**
     * Injection to init the vanilla recipe type registry in place of fake GT one
     */
    @ModifyArg(method = "postRegisterEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;get(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;"))
    private static ResourceLocation gtceu$loadVanillaRecipeTypeRegistryInstead(ResourceLocation registryName) {
        if (registryName.equals(GTRegistries.Keys.RECIPE_TYPE.location())) {
            return Registries.RECIPE_TYPE.location();
        }
        return registryName;
    }
}
