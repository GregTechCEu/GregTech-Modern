package com.gregtechceu.gtceu.core.mixins.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.CommonProxy;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.GameData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(value = GameData.class, remap = false)
public class GameDataMixin {

    // Make GT register events fire first, even before minecraft registries.
    @ModifyVariable(
                    method = "postRegisterEvents",
                    at = @At(
                             value = "INVOKE",
                             target = "Ljava/util/Set;addAll(Ljava/util/Collection;)Z",
                             ordinal = 1,
                             shift = At.Shift.AFTER),
                    name = "ordered")
    private static Set<ResourceLocation> gtceuFirst(Set<ResourceLocation> ordered) {
        return ordered.stream()
                .sorted((a, b) -> {

                    // fire the material registry first
                    if (a.equals(GTCEu.id("material"))) return -1;
                    if (b.equals(GTCEu.id("material"))) return 1;

                    boolean aGt = a.getNamespace().equals("gtceu");
                    boolean bGt = b.getNamespace().equals("gtceu");
                    if (aGt && !bGt) return -1;
                    if (!aGt && bGt) return 1;
                    return 0;
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Inject(method = "postRegisterEvents", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/ModLoader;postEventWrapContainerInModOrder(Lnet/minecraftforge/eventbus/api/Event;)V", shift = At.Shift.AFTER))
    private static void postLateRegistryEvent(CallbackInfo ci, @Local(name = "registryKey") ResourceKey<? extends Registry<?>> registryKey) {
        //CommonProxy.onRegisterLate(registryKey);
    }
}
